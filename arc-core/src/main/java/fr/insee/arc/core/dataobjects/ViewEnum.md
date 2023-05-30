# Example of query to simply get ViewEnum java definition from database

```sql=
select ', '||upper(table_name)||'("'||table_name||'", SchemaEnum.METADATA,'||string_agg('ColumnEnum.'||upper(column_name),', ' order by ordinal_position)||') //' as l
from information_schema.columns
where table_name like 'ihm%'
and table_schema='arc'
and table_catalog='arc_prod'
group by table_name
```