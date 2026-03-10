# Example of query to simply get the ColumnEnum java definition from database

```sql=
select distinct ', '||upper(column_name)||'("'||column_name||'", TypeEnum.'||upper(data_type)||', "") // '||string_agg(table_name,',') over (partition by column_name) as l
from information_schema.columns
where true 
and table_name like 'ihm%'
and table_schema='arc'
and table_catalog='arc_prod'
```
