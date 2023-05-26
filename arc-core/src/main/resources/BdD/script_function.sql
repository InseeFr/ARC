-- script fonctions

DROP FUNCTION IF EXISTS arc.transpose_pilotage_calcul() CASCADE;
DROP FUNCTION IF EXISTS arc.update_list_param(n text, dt text) CASCADE;
DROP FUNCTION IF EXISTS arc.transpose_pilotage_fin() CASCADE;
DROP FUNCTION IF EXISTS arc.verif_doublon() CASCADE;

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

-- arc schema business function
CREATE OR REPLACE FUNCTION arc.fn_check_calendrier() 
RETURNS boolean AS 
$BODY$ 
DECLARE 
n integer; 
BEGIN  
select count(1) into n from arc.ihm_calendrier a 
where exists (select 1 from arc.ihm_calendrier b 
where a.validite_inf>=b.validite_inf  
and a.validite_inf<=b.validite_sup  
and a.id_norme=b.id_norme  
and a.periodicite=b.periodicite 
and a.etat='1' 
and a.etat=b.etat 
and a.id<>b.id); 
      	if n>0 then  
RAISE EXCEPTION 'Chevauchement de calendrier'; 
end if; 
select count(1) into n from arc.ihm_calendrier a 
where a.validite_inf>a.validite_sup; 
      	if n>0 then  
RAISE EXCEPTION 'Intervalle non valide. Date inf >= Date sup'; 
end if; 
return true; 
END;  
$BODY$ 
LANGUAGE plpgsql VOLATILE 
COST 100; 
        
        
CREATE OR REPLACE FUNCTION arc.fn_check_jeuderegle() 
RETURNS boolean AS 
$BODY$ 
DECLARE 
  n integer; 
BEGIN  
SELECT count(1) into n 
FROM 	( 
SELECT id_norme, periodicite, validite_inf, validite_sup, etat, count(etat) 
FROM arc.ihm_jeuderegle  b 
WHERE b.etat != 'inactif' 
GROUP BY id_norme, periodicite, validite_inf, validite_sup, etat 
HAVING count(etat)>1 
) AS foo; 
if n>0 then  
RAISE EXCEPTION 'Un seul jeu de règle en production ou par bac à sable pour un calendrier'; 
end if; 
return true; 
END;  
$BODY$ 
LANGUAGE plpgsql VOLATILE 
COST 100; 
        
        
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


CREATE OR REPLACE FUNCTION arc.insert_controle() 
RETURNS trigger AS 
$BODY$ 
DECLARE i integer; 
nom_table text := quote_ident(TG_TABLE_SCHEMA) || '.'|| quote_ident(TG_TABLE_NAME); 
query text; 
  BEGIN 
if (new.id_regle is null) then 
query:='select coalesce(max(id_regle),0)+1 from '||nom_table||' 
where id_norme='||quote_literal(NEW.id_norme)||'  
and periodicite='||quote_literal(NEW.periodicite)||'  
and validite_inf='||quote_literal(NEW.validite_inf)||'  
and validite_sup='||quote_literal(NEW.validite_sup)||'   
and version='||quote_literal(NEW.version)||' '; 
EXECUTE query INTO i; 
NEW.id_regle:=i; 
end if; 
RETURN NEW; 
  END; 
$BODY$ 
LANGUAGE plpgsql VOLATILE 
COST 100; 



CREATE OR REPLACE FUNCTION public.array_agg_distinct_gather(
    tab cle_valeur[],
    src cle_valeur)
  RETURNS cle_valeur[] AS
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


CREATE OR REPLACE FUNCTION public.array_agg_distinct_result(cle_valeur[])
  RETURNS text[] AS
$BODY$ 
 -- select array_agg(v) from (select m.v from unnest($1) m where m.i is not null order by m.i, m.v ) t0 
  select array_agg(v) from (select m.v from unnest($1) m order by m.i, m.v ) t0 
 $BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 100;

do $$ begin
CREATE AGGREGATE public.array_agg_distinct(cle_valeur) (
  SFUNC=array_agg_distinct_gather,
  STYPE=cle_valeur[],
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

CREATE OR REPLACE FUNCTION public.explain(
    sql1 text,
    sql2 text)
  RETURNS text AS
$BODY$
declare a text:='';
DECLARE cur refcursor;
DECLARE c record;

begin

execute sql1;

open cur for execute sql2;
loop
   FETCH cur INTO c;
   EXIT WHEN NOT FOUND;
   a:=a||c::text||chr(13);
end loop;
close cur;

return a;

end;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

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