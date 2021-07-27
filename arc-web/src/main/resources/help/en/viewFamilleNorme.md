# Norm family management
{lang=en}

## Norm family presentation

The goal of the Arc application is to integrate in a information system data with a stable model from files with a model that can change over time. This stability make simplier the statistical process of the data because you just have to change how you output variables are compute.

One a the best example is the administrative data. It's common that administrative files change over time. Not a big change, but some variables are deleted, renamed, or created. But for your process you maybe need the same tables and variables names every year.

So the norm family work as a metamodel of output. It determine :

- The output tables
- The links between the tables
- The output variables : name and type

For instance, every month you get administrative files about tax named TAXE_FILE and another about salary name SALARY. Those administrative file are not made for statistical process, so you use ARC to make their more "statistic friendly". Those two type of file will be used in different statistical process so you create two norm families, one for the tax files named "TAX" and the other for the salary files named "SALARY". For those two families you will configure the output tables and variables but not how they are computed.