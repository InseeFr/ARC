CREATE OR REPLACE FUNCTION arc.update_list_param(
    n text,
    dt text)
  RETURNS boolean AS
$BODY$
DECLARE p text;
BEGIN
begin
	p:=current_setting(n);
	exception when others then 
	perform set_config(n,';'||dt||';',true );
	return false;
end;

if (p='') then
	perform set_config(n,';'||dt||';',true );
	return false;
end if;

if (p not like '%;'||dt||';%') then
	perform set_config(n,p||dt||';',true );
end if;

return true;
END;
$BODY$
LANGUAGE plpgsql VOLATILE
COST 100;

-- Function: arc.transpose_pilotage_calcul()
-- DROP FUNCTION arc.transpose_pilotage_calcul();
CREATE OR REPLACE FUNCTION arc.transpose_pilotage_calcul()
  RETURNS trigger AS
$BODY$
DECLARE dt text;
DECLARE b boolean;
DECLARE c integer;
BEGIN

if (TG_OP='UPDATE') then
	if (old.phase_traitement=new.phase_traitement and old.etat_traitement=new.etat_traitement) then
	return null;
	end if;
end if;

if (TG_OP in ('UPDATE','DELETE')) then
	dt:='p.'||old.date_entree||'_'||old.phase_traitement||'_'||array_to_string(old.etat_traitement,'$');
	b:=arc.update_list_param('p.pilotage',dt);
		begin
		if (current_setting(dt)='') then c=-1; else c:=current_setting(dt)::integer-1; end if;
		exception when others then 
		c:=-1;
		end;
		perform set_config(dt,c::text,true );
end if;

if (TG_OP in ('UPDATE','INSERT')) then
	dt:='p.'||new.date_entree||'_'||new.phase_traitement||'_'||array_to_string(new.etat_traitement,'$');
	b:=arc.update_list_param('p.pilotage',dt);
		begin
		if (current_setting(dt)='') then c=1; else c:=current_setting(dt)::integer+1; end if;
		exception when others then 
		c:=1;
		end;
		perform set_config(dt,c::text,true);
end if;



return null;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

-- Function: arc.transpose_pilotage_fin()

-- DROP FUNCTION arc.transpose_pilotage_fin();

CREATE OR REPLACE FUNCTION arc.transpose_pilotage_fin()
  RETURNS trigger AS
$BODY$ 
	 DECLARE bac text; 
	 DECLARE env text; 
	 DECLARE query text; 
	 DECLARE cols text[]; 
	 DECLARE cols_t text[]; 
	 DECLARE date_entree text; 
	 DECLARE dt text; 
	 declare b boolean:=false; 
	 BEGIN 
	 query:=''; 
	 
	 -- récupération des données d'environnement 
	 if (strpos(TG_TABLE_SCHEMA,'_')=0) then 
	 bac:=substr(TG_TABLE_NAME,1,strpos(TG_TABLE_NAME,'_')); 
	 env:=TG_TABLE_SCHEMA||'.'||bac; 
	 else 
	 bac:=''; 
	 env:=TG_TABLE_SCHEMA||'.'; 
	 end if; 
	 
	 
	 begin 
	 cols:=string_to_array(trim(current_setting('p.pilotage'),';'),';'); 
	 exception when others then 
	 return null; 
	 end; 
	 
	 if (current_setting('p.pilotage')='') then return null; end if; 
	 
	 
	 -- récupération des colonnes de la table de pilotage_t  
	 SELECT array_agg(column_name::text) into cols_t 
	 FROM   information_schema.columns 
	 WHERE  table_schema = TG_TABLE_SCHEMA 
	 AND    table_name = bac||'pilotage_fichier_t'; 
	 
	 --raise notice '%',cols_t; 
	 -- créer la table de pilotage si elle n'existe pas 
	 if (cols_t is null) then 
	 query:=query||' 

		"\n create table '||env||'pilotage_fichier_t(date_entree text) " + FormatSQL.WITH_NO_VACUUM + ";'; 
	 cols_t=array_append(cols_t, 'date_entree'); 
	 end if; 
	 
	 -- création de la requete qui va mettre à jour la table pilotage_t  
	 for i in 1..array_length(cols, 1) loop  
	 --raise notice '%',cols[i]; 
	 date_entree:=substr(cols[i],3,13); 
	 dt:=substr(cols[i],17); 
	 
	 -- si la colonne n'existe pas dans la table pilotage_t, on la crée  
	 if (not(lower(dt)= ANY (cols_t))) then 
	 query:=query||' 
	 alter table '||env||'pilotage_fichier_t add column '||dt||' integer default 0;'; 
	 cols_t=array_append(cols_t, lower(dt)); end if; 
	 
	 if (current_setting(cols[i])::integer!=0) then  

		 query:=query||'WITH upsert AS (update '||env||'pilotage_fichier_t set '||dt||'='||dt||'+'||current_setting(cols[i])||'  
	 where date_entree='''||date_entree||''' returning *)  

		INSERT INTO '||env||'pilotage_fichier_t (date_entree, '||dt||') SELECT '''||date_entree||''', '||current_setting(cols[i])||'  
	 WHERE NOT EXISTS (SELECT * FROM upsert); ';  
	 b:=true;  
	 perform set_config(cols[i],'0',true );  
	 end if; 
	 
	 end loop; 
	 
	 if (b) then 

		 query:=query||'delete from '||env||'pilotage_fichier_t where 0'; for i in 1..array_length(cols_t,1) loop 

		 if (cols_t[i]!='date_entree') then query:=query||'+coalesce('||cols_t[i]||',0)'; end if; end loop; query:=query||'=0;'; end if; 
	 
	 --raise notice '%',query; 
	 execute query; 
	 
	 return null; 
	 END; 
	 $BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;


do $$ begin create type public.cle_valeur as (
  i bigint,
  v text collate "C"); exception when others then end; $$; 
         CREATE OR REPLACE FUNCTION public.distinct_on_array(text[])
  RETURNS text[] AS
$BODY$ 
 select array_agg((m).v) from (select m::public.cle_valeur from (select distinct m from unnest($1) m) t0 ) t1  where (m).i is not null 
 $BODY$
  LANGUAGE sql IMMUTABLE STRICT
  COST 100;

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
 RAISE EXCEPTION 'Un seul jeu de règle en production ou par bac àsable pour un calendrier'; 
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
 IF TO_CHAR(TO_DATE($1,$2), $2) != $1 THEN 
 RETURN FALSE; 
 END IF; 
 RETURN TRUE; 
 EXCEPTION WHEN others THEN 
 RETURN FALSE; 
 END; 
 $BODY$ 
 LANGUAGE plpgsql IMMUTABLE 
 COST 100;
         
CREATE OR REPLACE FUNCTION arc.verif_doublon()
RETURNS trigger AS
$BODY$
DECLARE
n integer;
k integer;
nom_table text := quote_ident(TG_TABLE_SCHEMA) || '.'|| quote_ident(TG_TABLE_NAME);
query text;
BEGIN 
--RAISE NOTICE 'Nom de la table : %', nom_table;
query := 'SELECT count(1) FROM (SELECT count(1)	FROM '|| nom_table ||' b WHERE id_classe=''CARDINALITE'' 
GROUP BY id_norme, periodicite, validite_inf, validite_sup, version, lower(rubrique_pere), lower(rubrique_fils)
HAVING count(1)>1) AS foo';
--RAISE NOTICE 'Query vaut : %', query;	
EXECUTE query INTO n;
query := 'SELECT count(1) FROM 	(SELECT count(1) FROM '|| nom_table ||' b WHERE id_classe IN(''NUM'',''DATE'',''ALPHANUM'')
GROUP BY id_norme, periodicite, validite_inf, validite_sup, version, lower(rubrique_pere)
HAVING count(1)>1) AS foo';
--RAISE NOTICE 'Query vaut : %', query;
EXECUTE query INTO k;
RAISE NOTICE 'Variable de comptage n : %', n;
RAISE NOTICE 'Variable de comptage k : %', k;
if n>0 then 
RAISE EXCEPTION 'Règles de CARDINALITE en doublon'; 
end if;
if k>0 then 
RAISE EXCEPTION 'Règles de format (NUM,DATE,ALPHANUM) en doublon'; 
end if;
RETURN NEW;
END; 
$BODY$
LANGUAGE plpgsql VOLATILE
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
where id_norme='''||NEW.id_norme||''' 
and periodicite='''||NEW.periodicite||''' 
and validite_inf='''||NEW.validite_inf||''' 
and validite_sup='''||NEW.validite_sup||'''  
and version='''||NEW.version||''' ';
EXECUTE query INTO i;
NEW.id_regle:=i;
end if;
RETURN NEW;
  END;
$BODY$
LANGUAGE plpgsql VOLATILE
COST 100;

-- add triggers on table rules
do $$ begin CREATE TRIGGER tg_insert_chargement BEFORE INSERT ON arc.ihm_chargement_regle FOR EACH ROW EXECUTE PROCEDURE arc.insert_controle(); exception when others then end; $$;
do $$ begin CREATE TRIGGER tg_insert_controle BEFORE INSERT ON arc.ihm_controle_regle FOR EACH ROW EXECUTE PROCEDURE arc.insert_controle(); exception when others then end; $$;
do $$ begin CREATE TRIGGER tg_insert_filtrage BEFORE INSERT ON arc.ihm_filtrage_regle FOR EACH ROW EXECUTE PROCEDURE arc.insert_controle(); exception when others then end; $$;

