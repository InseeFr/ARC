# Norm rules

## What is a norm ?
A norm categories a file and identifies the processing pipeline to execute on the file. When a file is acquired by ARC for the first time, ARC tests all the active rules define in the norm wiew to see which one matches and ARC marks the file with its norm name and meta-informations as periodicity and validity.

### how it works ?
The norm calculation is the first step the "LOAD" module.
- ARC makes a first parse of the file line by line and store the result in a table called "alias-table". Please see its content below.
- The user has written some sql rules based on this table to retrieve some useful informations to identify the norm and the date of data
- ARC executes all the active user rules on every file to identify, for each file, which norm matches and what is the date of data
- If a single norm is found for the file then ARC marks the norm and the date of data for the file. If several norms or no norm had been found for the file, the file is put in the error "KO" stack


"alias-table" content

| id_source                        | id              | ligne                                            |
| -------------------------------- | --------------- | ------------------------------------------------ |
| the file name                    | the line number | the line content                                 |
|                                  |                 |                                                  |
| default_population.xml.tgz       | 1               | &lt;xml&gt;                                      |
| default_population.xml.tgz       | 2               | &nbsp;&lt;year&gt;2020&lt;&#47;year&gt; |
| default_population.xml.tgz       | 3               | &nbsp;&lt;countries&gt; |
| default_population.xml.tgz       | 4               | &nbsp;&nbsp;&lt;country&gt; |
| default_population.xml.tgz       | 5               | &nbsp;&nbsp;&nbsp;&lt;name&gt;FRANCE&lt;&#47;name&gt; |
| default_population.xml.tgz       | 6               | &nbsp;&nbsp;&nbsp;&lt;population&gt;67.39M&lt;&#47;population&gt; |
| default_population.xml.tgz       | 7               | &nbsp;&nbsp;&nbsp;&lt;source&gt;INSEE&lt;&#47;population&gt; |
| default_population.xml.tgz       | 8               | &nbsp;&nbsp;&lt;&#47;country&gt; |
| default_population.xml.tgz       | 9               | &nbsp;&nbsp;&lt;country&gt; |
| default_population.xml.tgz       | 10              | &nbsp;&nbsp;&nbsp;&lt;name&gt;ITALIA&lt;&#47;name&gt; |
| default_population.xml.tgz       | 11              | &nbsp;&nbsp;&nbsp;&lt;population&gt;59.55M&lt;&#47;population&gt; |
| default_population.xml.tgz       | 12              | &nbsp;&nbsp;&nbsp;&lt;source&gt;ISTAT&lt;&#47;population&gt; |
| default_population.xml.tgz       | 13              | &nbsp;&nbsp;&lt;&#47;country&gt; |
| default_population.xml.tgz       | 14              | &nbsp;&lt;&#47;countries&gt; |
| default_population.xml.tgz       | 15              | &lt;&#47;xml&gt; |


## The norm rules
## norm family
@Selector @Value: from the values declared in the norm family management screen
The target data model identifier for the norm

## norm name
@String

A unique name identifier for the norm

## periodicity
@Value : A, M

Meta-information about the periodicity of the file data. Annual, or monthly file ?

## norm calculation
@SQL expression : norm matches if at least one record is returned by the sql expression

For a given file, all the active norm registered are evaluated with the SQL expression set in the "norm calculation". If this expression returns any line, it is considered that the file match the norm.
Moreover, 
- if the file match several active norms, it will be marked as KO and it won't be processed to the next pipepline step
- if the file match one and only one active norm, ARC will mark the file with the norm id and compute the file "validity" according to the validity rule and the file "periodicity"


##  validity calculation
@SQL expression : must return a string value in the YYYY-MM-DD date format

The validity rule are computed to provide a date meta-information on a file. File validity will be compared to the calendar rules by ARC to know which pipeline we must used according to the file date meta-information (see calendar).

### examples

#### example 1 : user fixed validity
```
-- Set the validity for the norm to a given date such as 2021-01-01

SELECT '2021-01-01'

-- result  : 2021-01-01
```

#### example 2 : validity computed from the file data

```sql=
-- Use the "year" tag in the alias-table to compute a date for the data of the file
-- In the alias-table in example before, the "year" can be extracted from the third line and convert to a YYYY-MM-DD date format

SELECT split_part(split_part(year, '>',2),'<',1)||'-01-01' FROM alias_table WHERE id=3

-- result  : 2020-01-01
```


## State
@Selector @Value : 0=inactive, 1=active

Disable or enable the norm calculation in ARC.


