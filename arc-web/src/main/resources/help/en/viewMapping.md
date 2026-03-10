# Phase de mapping
{lang=fr}

## Description des règles

## Variables reservées
ArcProcessingTable : le nom de la table sur laquelle travaille le mapping; cela permet de créer des regles permettant de calculer une variable différemment selon la table contenant la variable

```sql=
case when ArcProcessingTable='mapping_majic_bati_00_ok' and #v_article#='00' then #i_00# when ArcProcessingTable='mapping_majic_bati_10_ok' and #v_article#='10' then #i_00# end
```

