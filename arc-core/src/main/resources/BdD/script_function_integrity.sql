-- script fonctions

DROP FUNCTION IF EXISTS arc.transpose_pilotage_calcul() CASCADE;
DROP FUNCTION IF EXISTS arc.update_list_param(n text, dt text) CASCADE;
DROP FUNCTION IF EXISTS arc.transpose_pilotage_fin() CASCADE;
DROP FUNCTION IF EXISTS arc.verif_doublon() CASCADE;

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
WHERE b.sandbox != 'inactif' 
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

CREATE OR REPLACE FUNCTION arc.fn_check_variable_famille() 
RETURNS trigger AS 
$BODY$ 
DECLARE 
n integer; 
begin
select max(c) into n from (
select 
case when max(row(type_variable_metier, description_variable_metier, type_consolidation)::text) =
min(row(type_variable_metier, description_variable_metier, type_consolidation)::text) then 0 else 1
end as c
from arc.ihm_mod_variable_metier
group by id_famille, nom_variable_metier
) u ;
if n>0 then  
RAISE EXCEPTION 'Variable incohérente'; 
end if;
RETURN NEW;
END;  
$BODY$ 
LANGUAGE plpgsql volatile;

