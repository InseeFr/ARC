CREATE SCHEMA IF NOT EXISTS arc;

-- optimization parameters at database level
-- dont forget to change parameters set at server nod level
/*
postgresql::server::config_entry:
  wal_compression:
    value: "on"
  force_parallel_mode:
    value: "off"
  log_statement:
    value: "none"
  max_locks_per_transaction:
    value: "1000"
  max_connections:
    value: "1000"
*/
do $$
declare
c record;
BEGIN
for c in (select current_database() as n) loop
execute 'alter database '||c.n||' set enable_bitmapscan=off;';
execute 'alter database '||c.n||' set synchronous_commit=off;';
execute 'alter database '||c.n||' set max_parallel_workers_per_gather=0;';
execute 'alter database '||c.n||' set max_parallel_maintenance_workers=0;';
execute 'alter database '||c.n||' set jit=off;';
end loop;
end;
$$;

-- fonctions technique sur les tableaux
do $$ begin create type public.cle_valeur as (i bigint, v text collate "C"); exception when others then end; $$;

-- multi dimensionnal array decomposition  
CREATE OR REPLACE FUNCTION public.decompose(ANYARRAY, OUT a ANYARRAY)
  RETURNS SETOF ANYARRAY AS
$func$
BEGIN
   FOREACH a SLICE 1 IN ARRAY $1 LOOP
      RETURN NEXT;
   END LOOP;
END
$func$  LANGUAGE plpgsql IMMUTABLE;  


-- global sequence and invokation function
do $$ begin CREATE SEQUENCE arc.number_generator cycle; exception when others then end; $$; 

        
CREATE OR REPLACE FUNCTION public.curr_val(text) 
  RETURNS bigint AS 
$BODY$ 
BEGIN 
return currval($1); 
exception when others then 
return nextval($1); 
END; 
$BODY$ 
LANGUAGE plpgsql VOLATILE 
COST 100; 

-- test if date        
CREATE OR REPLACE FUNCTION arc.isdate( 
text, 
text) 
RETURNS boolean AS 
$BODY$ 
BEGIN 
IF TO_CHAR(TO_TIMESTAMP($1,$2), $2) != $1 THEN 
RETURN FALSE; 
END IF; 
RETURN TRUE; 
EXCEPTION WHEN others THEN 
RETURN FALSE; 
END; 
$BODY$ 
LANGUAGE plpgsql IMMUTABLE 
COST 100;

CREATE OR REPLACE FUNCTION public.isdate( 
text, 
text) 
RETURNS boolean AS 
$BODY$ 
BEGIN 
IF TO_CHAR(TO_TIMESTAMP($1,$2), $2) != $1 THEN 
RETURN FALSE; 
END IF; 
RETURN TRUE; 
EXCEPTION WHEN others THEN 
RETURN FALSE; 
END; 
$BODY$ 
LANGUAGE plpgsql IMMUTABLE 
COST 100; 

-- aggregate on array
CREATE OR REPLACE FUNCTION public.array_agg_distinct_gather(
    tab public.cle_valeur[],
    src public.cle_valeur)
  RETURNS public.cle_valeur[] AS
$BODY$
DECLARE
BEGIN

if (src.i is null) then return tab; end if;
if (tab is null) then return array[src]; end if;
for k in 1..array_length(tab,1) loop
if (tab[k]).i=src.i then return tab; end if;
end loop;

return tab||src;

END;
$BODY$
  LANGUAGE plpgsql IMMUTABLE
  COST 100;


CREATE OR REPLACE FUNCTION public.array_agg_distinct_result(public.cle_valeur[])
  RETURNS text[] AS
$BODY$ 
 -- select array_agg(v) from (select m.v from unnest($1) m where m.i is not null order by m.i, m.v ) t0 
  select array_agg(v) from (select m.v from unnest($1) m order by m.i, m.v ) t0 
 $BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 100;

do $$ begin
CREATE AGGREGATE public.array_agg_distinct(public.cle_valeur) (
  SFUNC=array_agg_distinct_gather,
  STYPE=public.cle_valeur[],
  FINALFUNC=array_agg_distinct_result
);
exception when others then end; $$;

CREATE OR REPLACE FUNCTION public.curr_val(text)
  RETURNS bigint AS
$BODY$ 
 BEGIN 
 return currval($1); 
 exception when others then 
 return nextval($1); 
 END; 
 $BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
  
  
CREATE OR REPLACE FUNCTION public.distinct_on_array(public.cle_valeur[]) 
RETURNS text[] AS 
$BODY$ 
select array_agg(v) from (select distinct m.i, m.v from unnest($1) m where m.i is not null order by m.i, m.v ) t0 
$BODY$ 
LANGUAGE sql IMMUTABLE STRICT 
COST 100; 

CREATE OR REPLACE FUNCTION public.distinct_on_array(text[])
RETURNS text[] AS
$BODY$ 
select array_agg((m).v) from (select m::public.cle_valeur from (select distinct m from unnest($1) m) t0 ) t1  where (m).i is not null 
$BODY$
LANGUAGE sql IMMUTABLE STRICT
COST 100;

drop function if exists public.explain;

CREATE OR REPLACE FUNCTION public.sum_evoluee(double precision[])
  RETURNS double precision AS
$BODY$
DECLARE
	res numeric := 0;
BEGIN
	--RAISE NOTICE 'Mon input : %', $1;
	SELECT sum(t) INTO res FROM (SELECT unnest($1) AS t) foo;
RETURN res;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

CREATE OR REPLACE FUNCTION public.sum_evoluee(double precision)
  RETURNS double precision AS
$BODY$
BEGIN
RETURN $1;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;


CREATE OR REPLACE FUNCTION public.timer(sql1 text)
  RETURNS interval AS
$BODY$
declare s timestamp with time zone;
declare e timestamp with time zone;
begin

s:=clock_timestamp();
execute sql1;
e:=clock_timestamp();

return age(s,e);

end;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

CREATE OR REPLACE FUNCTION public.upper_without_special_char(text)
  RETURNS text AS
$BODY$
      SELECT upper(regexp_replace(
        regexp_replace(
          translate($1,
            'ÁÀÂÄĄȺǍȦẠĀÃ'
            ||'ĆĈÇȻČĊ'
            ||'ÉÈÊËȨĘɆĚĖẸĒẼ'
            ||'ÍÌÎÏĮƗǏİỊĪĨ'
            ||'ĴɈ'
            ||'ĹĻŁȽĽḶ'
            ||'ŃǸŅŇṄṆÑ'
            ||'ÓÒÔÖǪØƟǑȮỌŌÕ'
            ||'ŚŜŞŠṠṢ'
            ||'ŢȾŦŤṪṬ'
            ||'ÚÙÛÜŲɄǓỤŪŨ'
            ||'ÝỲŶŸɎẎỴȲỸ'
            ||'ŹẐƵŽŻẒ'
            ||'áàâäąⱥǎȧạāã'
            ||'ćĉçȼčċ'
            ||'éèêëȩęɇěėẹēẽ'
            ||'íìîïįɨǐịīĩ'
            ||'ĵɉǰ'
            ||'ĺļłƚľḷ'
            ||'ńǹņňṅṇñ'
            ||'óòôöǫøɵǒȯọōõ'
            ||'śŝşšṡṣ'
            ||'ẗţⱦŧťṫṭ'
            ||'úùûüųʉǔụūũ'
            ||'ýỳŷÿɏẏỵȳỹ'
            ||'źẑƶžżẓ'
            ||'''-&#@$*%/',
            'AAAAAAAAAAA'
            ||'CCCCCC'
            ||'EEEEEEEEEEEE'
            ||'IIIIIIIIIII'
            ||'JJ'
            ||'LLLLLL'
            ||'NNNNNNN'
            ||'OOOOOOOOOOOO'
            ||'SSSSSS'
            ||'TTTTTT'
            ||'UUUUUUUUUU'
            ||'YYYYYYYYY'
            ||'ZZZZZZ'
            ||'aaaaaaaaaaa'
            ||'cccccc'
            ||'eeeeeeeeeeee'
            ||'iiiiiiiiiii'
            ||'jj'
            ||'llllll'
            ||'nnnnnnn'
            ||'oooooooooooo'
            ||'ssssss'
            ||'tttttt'
            ||'uuuuuuuuuu'
            ||'yyyyyyyyy'
            ||'zzzzzz'
            ||'         '),
        ' +', ' ', 'g'),
      ' ?- ?', '-', 'g')
    );
  $BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 100;

-- functions to check sql injection
CREATE OR REPLACE PROCEDURE public.safe_select(query text)
AS
$BODY$
begin
perform check_no_injection(query);
EXECUTE FORMAT('DROP TABLE IF EXISTS safe_select; CREATE TEMPORARY TABLE safe_select AS %s',query);
END; 
$BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.check_no_injection(query text) RETURNS boolean
as
$BODY$
declare query_analyze text:=regexp_replace(query,'''[^'']+''','','g');
begin
if (strpos(query_analyze,';')>0 or strpos(query_analyze,'--')>0 or strpos(query_analyze,'/*')>0 or strpos(query_analyze,'*/')>0)
then
RAISE EXCEPTION 'SQL with multiple statements or comments are forbidden to prevent injection'; 
end if;
return true;
END; 
$BODY$
LANGUAGE plpgsql;

  
-- grant / revoke
REVOKE ALL ON SCHEMA public FROM public;
REVOKE ALL ON SCHEMA arc FROM public; 

-- restricted role for service execution
do $$ begin
if ('{{userRestricted}}'!='') then 
	execute 'CREATE ROLE {{userRestricted}} with NOINHERIT;';
end if;
exception when others then end; $$;

do $$ begin
if ('{{userRestricted}}'!='') then 
	execute 'GRANT USAGE ON SCHEMA public TO {{userRestricted}}; GRANT EXECUTE ON ALL ROUTINES IN SCHEMA public to {{userRestricted}}; GRANT USAGE ON SCHEMA arc TO {{userRestricted}}; GRANT EXECUTE ON FUNCTION arc.isdate to {{userRestricted}}; GRANT USAGE, SELECT, UPDATE ON SEQUENCE arc.number_generator to {{userRestricted}};';
end if; 
exception when others then end;
$$;
