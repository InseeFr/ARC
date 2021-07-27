# Load rules
{lang=en}

## Rules description

### Format rules

```xml=
<encoding>UTF8</encoding>
<quote>E'\2'</quote>
<headers>col</headers>
<join-table>mapping_esane_entete_ok
<join-type>inner join
<join-select>*
<join-clause>substring(v_col,1,24)=v_depot_entete and substr(substr(id_source,strpos(id_source,'_')+1),1,5)=substr(substr(v_id_source,strpos(v_id_source,'_')+1),1,5) and substr(substr(id_source,strpos(id_source,'_')+1),7)=substr(substr(v_id_source,strpos(v_id_source,'_')+1),7)
<join-table>arc.nmcl_corresp_v004
<join-type>inner join
<join-select>id_insee,regime,nref,formulaire,format_dgfip,ordre_doublon
<join-clause>v_regime_entete=v_regime and substring(v_col,58,6)=v_nref and trim(substring(v_col,25,20))=v_formulaire
<partition-expression>v_cle_entete
i_col=#pn#+dense_rank() over (order by v_cle_entete)
v_col=rtrim(v_col)
i_choice_fiscal=null::int
v_choice_fiscal=row_number() over (partition by v_cle_entete,v_id_insee order by v_ordre_doublon)
<where>v_choice_fiscal$new$=1
```