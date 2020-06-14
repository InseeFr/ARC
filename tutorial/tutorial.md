---
tags: ARC, INSEE
title: ARC tutorial
author: Manuel Soulier (INSEE)
---

# ARC tutorial

### Load CSV files in simple data models

---

### 1) The animal sample files

---

### [animals-001.csv](https://github.com/InseeFr/ARC/blob/master/tutorial/samples/animals-001.csv)
<span/>

* it is a zoo tycoon 1 files

* Columns and data structure
<span/>

Animal|Terrain|Status|Cost|Location/Era|
|-|-|-|-|-|
|Ankylosaurus|Savannah|Extinct|$2,700|North America (Cretaceous)|
|Triceratops|Savannah|Extinct|$4,000|North America (Cretaceous)|

<span/>

* it is a 8859-1 ANSI encoded file

* Location and Era are concatenated

---

### [animals-002.csv](https://github.com/InseeFr/ARC/blob/master/tutorial/samples/animals-002.csv)
<span/>

* it is zoo tycoon 2 files

* Columns and data structure
<span/>

|animal|biome|status|popularity|cost|location|
|-|-|-|-|-|-|
|Grizzly Bear|Boreal Forest|Endangered|3.5|$15,000|North America|
|Polar Bear|Tundra|Low risk|3|$10,000|Arctic|

<span/>

* it is an UTF-8 encoded file

* As compared to the zoo tycoon 1 files

    * there isn't any era information

    * "terrain" had been renamed to "biome"

    * there is a "popularity" information

---

### [animals-003-aquatic.csv](https://github.com/InseeFr/ARC/blob/master/tutorial/samples/animals-003-aquatic.csv)

* Well, it is a file ...

* Let's see if ARC is able to know from what version of the Zoo Tycoon game it is from

---

### What is the deal ?

1. We will set a target data model. This data model will store the data of these 2 different files the way we want.

2. We will set a first pipeline to load the animal from the first type of files
    - these files from the game zoo tycoon 1 are called "animals+ something", they are in ANSI encoding and don't have a "popularity" information

3. We will test our pipeline by loading all the files and see what happen

5. We will then define a second pipeline to load the files from the game zoo tycoon 2
    - these files from the game zoo tycoon 2 are UTF8 encoding and have a "popularity" information

6. We will change both the data model and the pipeline to host some new data

7. We will test our pipeline by loading all the files again and see what happen

8. We will finally change the data model to structure our file data in a two tables normalized model and work again on the pipeline
    - a "biome" table and an "animal" table
    - each animal will be linked to a single biome

---

### 2) Set a target data model

---

### A simple 1-table data model
<span/>

* We want to load the file in a data model called "bio"

* Structure of a "bio" data model

    - Table #1 "animal"
<span/>

|animal_name|cost_in_euros|conservation_status|
|-|-|-|
|text|float|text|

---

### Create the data model in ARC
<span/>

* A data model is called a "norm family" in Arc

> In the main menu > click on "Norm family managment"
>>

* Add a new norm family called "bio" in "Norm Families" table

> In the "Norm Families" section
>> 1- Click in the cell right of the "+"
>> 2- Write "bio"
>> 3- Press Ctrl+Enter to validate the entry or click on "Add" button
>> The "Ctrl+Enter" hotkey execute the default action associated with the cell

* Add a new norm family called "test" in "Norm Families" table

* Delete the "test" norm family entry

> Click the line checkbox for and click on "delete"
>>

---

### Create the tables of a data model
<span/>

* A table associated to a data model must check the following format
    * mapping_<data_model_name>_<table_name>_ok
    
* Create the "animal" table associated to the data model "bio"
> In the "Model tables" section
>> 1- Add an entry "mapping_bio_animal_ok"

<span/>

* A new column called "animal" will appear in the "data model fields" section 

---

### Add fields to the data model tables #1
<span/>

* Insert the animal_name field in the table "animal"
> In the "data model fields" section
>> 1. in "Field name", write "animal_name". Use only lower case and no space. Only dollar and underscore are allowed as separator as it must be database compliant
>> 2. in "Type", select "text". It is the database type of the field to be created
>> 3. in "Comment", write an optionnal comment that describe the field to be created
>> 4. in "animal", write a "x" (or whatever you want). It means that the field animal_name now belongs to the table animal

* Insert the other fields cost_in_euros (bigint as type) and conservation_status (text as type)

* Insert the two mandatory fields id_source and id_{table_name}
    * id_source is the name of the file. It is a text and must be belongs to every table of the data model.
    * id_animal is the primary key of the table "animal". These id_{table_name} are bigint and must belong to their main table {table_name} and may belong to others table as foreign key

---

### Add fields to the data model tables #2
<span/>

* View of the "data model fields" section for the "bio" data model
<span/>

|Field Name|Field Type|Comment|animal|
|-|-|-|-|
|-|-|-|-|
|animal_name|text|Name of the animal|x|
|cost_in_euros|float|Value of the animal in euros|x|
|conservation_status|text|Conservation status of the animal|x|
|id_source|text|Name of the original file|x|
|id_animal|bigint|Animal table primary key|x|
|game_name|text|The name of the game where the animal data comes from|x|

<span/>

* Filter the view to display the Field names containing the text "id"
>> 1. Write "id" in the cells just below the "Field Name" header
>> 2. Press Ctrl+Enter to trigger the filter action

<span/>

* Sort the view by Field Type
>> 1. Click on the "Field Type" header to sort the view on the header ascending.
>> 2. Click again to sort descending.

---

### 3) Set an ARC processing pipeline

---

### Create a norm (1/3)

* In ARC, a processing pipeline is identified by a "norm"
> In the main menu > click on "Norm managment"
>>

<span></span>
* Create an active norm "ZT1_V001" to load the animal files in the bio data model
> Fill the cells of "+" line of the "Norm definition" section as follow
>> 1. in "Norm family", choose the "bio" data model created before
>> It means that the target of the processing pipeline is the "bio" data model
>> 2. in "Norm", choose a name for your processing pipeline. I choosed ZT1_V001 (like Zoo Tycoon 1 version 001)
>> 3. in "Periodicity", choose a periodicity of your file. At the moment, only "Annual" or "Monthly" can be selected but more can be added. I choosed Annual. Note that is is only a metadata information that is not really used by the pipeline.
>> 4. in "State", choose "ACTIF". It means that the norm is active. A norm can be disabled by choosing "INACTIF"

---

### Create a norm (2/3)

* Overview on the "Norm calculation"

    * In "Norm calculation", a SQL select query must be written
   
   * For each file loaded, this query is evaluated. If it returns any records, ARC will know this norm and this pipeline will have to be used for the file. This "Norm calculation" rules must be exclusive between all norms and musn't overlap either ARC will generate an error.

    * The query may use a table called "alias_table". Each file loaded by ARC is read row by row and the content is temporary stored in the a table called "alias_table".

    * alias_table got 3 columns
        * id_source (the file name)
        * id_ligne : the row number. It starts from 0
        * ligne : the row data

* When ARC loads the file [animals-zt1.csv](https://github.com/InseeFr/ARC/blob/master/tutorial/samples/animals-zt1.csv) from the "DEFAULT" warehouse, the "alias_table" generated looks like :

|id_source|id_ligne|ligne|
|-|-|-|
|DEFAULT_animals-zt1.csv|0|African Elephant;Savannah;Vulnerable;$2,500;Africa|
|DEFAULT_animals-zt1.csv|1|Olive Baboon;Savannah;Least Concern;$900;Africa|
|DEFAULT_animals-zt1.csv|2|Plains Zebra;Savannah;Near Threatened;$800;Africa|
|...|||

---

### Create a norm (3/3)
> Let's fill the remaining cells of the "Norm definition" before adding it in ARC
>>

* Write a rule to check that the file contains "animal" in its filename and that the token "popularity" is NOT in its first row in lowercase
> 5. In “Norm calculation”, write the sql query
>> 
>> SELECT 1 FROM alias_table WHERE id_source LIKE '%animal%'
AND NOT EXISTS (SELECT 1 FROM alias_table WHERE id_ligne=1 AND lower(ligne) LIKE '%;popularity;%')

<span/>

* Overview of the "Validity calculation"
    * The "Validity" provides a date information about the file.
    * The "Validity calculation" rule is an SQL query that must return a date text in 'YYYY-MM-DD' format.
    * This SQL query may use the table "alias_table" so the date information can be calculated from the data or the name of the file.

<span/>


> 6. In "Validity calculation", write a sql query to return '2020-06-01'
>>
>> SELECT '2020-06-01'

<span/>

> 7. Click the "Add" button below the "Norm definition" section or press Ctrl-Enter
>> - The new line correponding to the norm created appears in the "Norm definition" section
>> - It can be updated by clicking on the cells to edit and click the "Update" button or press Ctrl-Enter
>> - It can be selected by clicking on the line checkbox. The selection opens the "Norms calendar" section
>> - It can be deleted by selecting it with the checkbox and click the "Delete" button

---

### Set the validity interval of the ZT1_V001 pipeline

<span/>

* The "Norm calendar" section is meant to be able to set different pipelines for different intervals of time
* The interval of time is compared to the "Validity" of the file to know which pipeline should be used

<span/>

* We want our pipeline will be valid from "2020-01-01" to "2025-01-01"

<span/>

> In the "Norm definition" section 
>> 1. Click on the norm checkbox to open its "Norms calendar" section

<span/>

> Fill the cells of "+" line of the "Norm calendar" section 
>> 1. in "Validity min", 2020-01-01
>> 2. in "Validity max", 2025-01-01
>> 3. in "State", choose "ACTIVE"
>> 4. Click the "Add" button below the section or press Ctrl-Enter



---

### Assign the ZT1_V001 pipeline to a sandbox
<span/>

* We want our pipeline to work on the sandbox 1

<span/>

> In the "Norm calendar" section 
>> 1. Click on the "norm calendar" checkbox to open its "Rulesets" section

<span/>

> Fill the cells of "+" line of the "Rulesets" section 
>> 1. in "Version", write "v001"
>> 2. in "State", select the "Bac à sable 1" (sandbox 1)
>> 3. Click the "Add" button below the section or press Ctrl-Enter

---

### Set the rules of the "LOAD" module rules for the ZT1_V001 pipeline

<span/>

* The "LOAD" module is one of the 6 processing modules implemented by ARC

<span/>

* We want to tell ARC that the file to be loaded by the ZT1_V001 pipeline are csv files with ";" separator and with an 8859-1 ANSI encoding

<span/>

> In the "Rulesets" section 
>> 1. Click on the "Rulesets" checkbox to open the pipeline steps section
>> 2. Click on the "Load" link to open the "Load rules" section

<span/>

> Fill the cells of "+" line of the "Load rules" section 
>> 1. in "Type of file", choose "plat" (flat file)
>> 2. in "Delimiter", write ;
>> 3. in "Format", write <encoding>ISO-8859-1</encoding>
>> 4. Click the "Add" button below the section or press Ctrl-Enter

---


### 4) Use sandbox to try your pipelines on your files

---

### Try the animal files in the sandbox 1

<span/>

* We will now try the pipeline ZT1_V001
> Enter the sandbox workbench
>> 1. Next to "Choose your working environment", select "BAS1" (sandbox1)
>> 2. Click on "Manage environment"

<span/>

* Reset the sandbox
> In the "Run a module" section
>> 1. Click on "Reset Sandbox"

<span/>

* Upload and register the 3 animal files into ARC
> In the "Register files" section, click on "Browse"
>> 1. Select all the files you want to upload to arc with the crtl key
>> ARC can read zip, gzip, tar and tar.gz archive

>> 2. Select the "DEFAULT" target warehouse

>> 3. Click on "Register"

> In the "Files status in the workflow" section
>> 1. 3 files are in the "register OK" state
>> 2. click on the cell "3". The details of the 3 files appear in the "Workflow files detail" section

---

### Run a module in ARC (1/2)

<span/>

* The "Run a module" section diplays from left to right a sequential chain of modules that constitutes the ARC processing.
> The step after "Register" is "Load". So the 3 files in the "Register OK" state are eligible to be processed by the "Load" module
> In the "Run a module" section
> > 1. Click on the "Load" button. It runs the "Load" module on the eligible files (the ones in a "Register OK" state).

<span/>

* What did happen ?
>In the "Files status in the workflow" section
>> 1. Two files are in a "Load OK" state, one file is in a "Load KO" state

>> 2. Click on the cell containing "2" referencing the "2" files in the "Load OK" status
>> It opens the "Workflow files detail" section with the 2 files details
>> 3. These 2 files had been marked by ARC as "ZT1_V001" norm, "2020-06-01" as validity and "A" as periodicity

>> 4. Click on the cell containing "1" referencing the file in the "Load KO" status
>> It opens or updates the "Workflow files detail" section with the file details
>> 5. The norm, validity and periodicity are empty. But there is a "Message report" : "java.lang.Exception: Zero norm match the expression".
>> 6. It means that ARC couldn't find any norm that matches this file and thus, it isn't a Zoo Tycoon 1 file as we defined them


---

### Run a module in ARC (2/2)

<span/>

* The files in the "Load OK" status are eligible to be processed by the next module called "Restructure"
> In the "Run a module" section
>> 1. Click on the "Restructure" button
>> 2. The 2 files in the "Load OK" status went through the "Restructure" process. They are now in a "Restructure OK" state and eligible for the next module "Control".

<span/>
* Execute a full processing chain 
> In the "Run a module" section
>> 3. Now execute the full processing chain remaining for the 2 files
>> 4. The 2 files stops at "Map KO". Indeed, we still hadn't set how ARC must map the file data into the "bio" data_model

<span/>

* How "undo" work ?
> In the "Run a module" section
>> 5. CLick on "undo Load" button to bring back all the 3 files in a "Register OK" state as if the "Load" module had never been processed.
>> 6. Click on the "Load" button to process the with "Load" module again

<span/>

* The "undo"/"do" operations make rules testings a lot easier : 1- write some new rules for a module, 2- undo the module, 3- process the module again to test the new rules

---

### Vizualize the modules output

> In the "Files status in the workflow" section
> 1. Click on the cell containing "2" referencing the "2" files in the "Load OK" status
>> It opens or updates the "Workflow files detail" section with the file details

> In the "Workflow files detail" section
>> 1. Click on "Download Data" 
>> 2. Open the csv file correponding to "DEFAULT_animals-001.csv"

<span/>

id_source|id|date_integration|id_norme|periodicite|validite|i_animal|v_animal|i_terrain|v_terrain|i_status|v_status|i_cost|v_cost|i_location_era|v_location_era
-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-
text|int4|text|text|text|text|int4|text|int4|text|int4|text|int4|text|int4|text
DEFAULT_animals-001.csv|1|2020-06-14|BIO_v001|A|2020-05-22|1|African Elephant|1|Savannah|1|Vulnerable|1|$2|500|1
DEFAULT_animals-001.csv|2|2020-06-14|BIO_v001|A|2020-05-22|2|Olive Baboon|2|Savannah|2|Least Concern|2|$900|2|Africa
DEFAULT_animals-001.csv|3|2020-06-14|BIO_v001|A|2020-05-22|3|Plains Zebra|3|Savannah|3|Near Threatened|3|$800|3|Africa

<span/>
* The content of "Location/Era" from the input file can now be found in the database column called "v_location_era"

<span/>

* ARC also added the meta data information to the table : id_norme, id_source, validite, ...

<span/>
* You will have to use this columns naming to write the rules of the next modules. Let's have an example to map our data in our "bio" data model.

---

### 5) Back to the ZT1_V001 pipeline rules

---


### Set the rules of the "Map model" for the ZT1_V001 pipeline (1/2)

* We want to tell ARC how to map the file data to our "bio" data model for ZT1_V001 pipeline. Let's go back to the "norm management" screen and proceed

<span/>

> In the main menu, click on the "Norm managment" link
>> 1. Select the "ZT1_V001" norm by clicking on its checkbox in the "Norms definition" section 
>> 2. Select the calendar by clicking on its checkbox in the "Norms calendar" section
>> 3. Select the ruleset correponding to "sandbox 1" by clicking on its checkbox in the "Rulesets" section
>> 4. Click on the "Map Model" link checkbox to open the "Mapping rules" section
>> 5. Click on "Generate a ruleset" in the "Map Model" section
>> It initializes empty rules with all the columns of our "bio" data that will have to be set

---

### Set the rules of the "Map model" for the ZT1_V001 pipeline (2/2)

<span/>

* Use SQL syntax to write the data model mapping expression

* Remember you must use the columns naming used by ARC to store data such as "v_animal", "v_location_era", "id_source", "id_norme", "validite", ...

* These column names must be escaped by brackets such as {v_animal}, {id_source}, ...

<span/>

> In the "Map Model" section, fill the "SQL expression" and optionally the "Comment" for the variables of the data model
>> 1. Click on the "SQL expression" cell or the "Comment" cell correponding to the variables, and write the rules
>> 2. Click on "Update" or press Ctrl+Enter to validate the entries
>> 3. Be careful when writing multiple cells at the same time, if the SQL expression is not correct, ARC rejects all the entries and they will be lost

<span/>

* Map to model rules should look like that

<span/>

|Field name|SQL expression|Comment|
|-|-|-|
|id_animal|{pk:mapping_bio_animal_ok}| this sql expression means id_animal is a serial number primary key for each file|
|id_source|{id_source}|The name of the file. Don't forget to use brackets !|
|animal_name|{v_animal_name}|The animal name as it is stored by ARC. v_ means "value"|
|cost_in_euros|regexp_replace({v_cost},'[^0123456789]','','g')::int&ast;0.89|Convert the v_cost in euros as an integer : keep only number digit, cast to integer and apply the conversion rate. SQL is powerful|
|conservation_status|{v_status}|TODO : change that to store modalities instead of plain text|
|game_name|case when {id_norme} like 'ZT1%' then 'Zoo Tycoon 1' end|We've built the norm to identify the game version of the data. That is a smart choice for our use case.|


---

### 5) Back to the sandbox 1


--- 

### Try our new rules

<span/>

> Enter the sandbox workbench
>> 1. Next to "Choose your working environment", select "BAS1" (sandbox1)
>> 2. Click on "Manage environment"

>> 3. Run the "Initialize" module
>> This module copies the new rules to the sandbox but also rebuilds the sandbox database and filesystem to a consistent state. This step is generally not needed to be started in GUI mode as ARC does it automatically when you start a module. Just good to know.

<span/>

>> 4. Run the required modules to try the "Map model" rules
>> 5. Download the output data of the "Map model OK" files to vizualize the output


---

# THE END... for now


```
Manuel Soulier
SNDI Orléans, INSEE
manuel.soulier@insee.fr
```

---


<style>


.reveal h3 {
    font-size: 0.8em;
}

.reveal.center .slides
{
    width: 75% !important;
}

.reveal blockquote ol
{
    margin: 0;
    padding: 0;
}


.reveal blockquote ol li
{
    color : #134A87 !important;
    font-size: 1.8rem;
    margin: 0.5rem 0;
}

.reveal blockquote ul
{
    margin: 0;
    padding: 0;
}


.reveal blockquote ul li
{
    color : #134A87 !important;
    font-size: 0.8em;
    margin: 0.5em 0;
    border:0;
}

space {
    margin-bottom:1em;
}


body {
    background-color:#fafafa !important;
}

ul, li
{
    color: #000000 !important;
    font-size:0.8em;
}

.reveal ul, .reveal li
{
    color: #000000 !important;
    font-size:2rem;
}

table, .reveal table
{
    color: #000000 !important;
    font-size:0.30em;
}

.reveal blockquote
{
    text-align: left;
    width:100%;
    padding: 0 5em;
    color: #777;
    border-left: .25em solid #ddd;
    font-size:0.4em;
    margin:0;
}

.reveal blockquote p:first-child
{
    font-size:1.2em;
    margin: 0.3em;
}

.reveal blockquote p:last-child {
    font-size:2rem;
    margin: 0.1em;
}



.reveal li > p
{
    color : #000000;

}

ul, .reveal ul
{
    display:block;
}

html, body, .reveal div, .reveal applet, .reveal object, .reveal iframe, .reveal h1, .reveal h2, .reveal h3, .reveal h4, .reveal h5, .reveal h6, .reveal p, .reveal pre, .reveal a, .reveal abbr, .reveal acronym, .reveal address, .reveal big, .reveal cite, .reveal code, .reveal del, .reveal dfn, .reveal em, .reveal img, .reveal ins, .reveal kbd, .reveal q, .reveal s, .reveal samp, .reveal small, .reveal strike, .reveal strong, .reveal sub, .reveal sup, .reveal tt, .reveal var, .reveal b, .reveal u, .reveal center, .reveal dl, .reveal dt, .reveal dd, .reveal ol, .reveal fieldset, .reveal form, .reveal label, .reveal legend, .reveal article, .reveal aside, .reveal canvas, .reveal details, .reveal embed, .reveal figure, .reveal figcaption, .reveal footer, .reveal header, .reveal hgroup, .reveal menu, .reveal nav, .reveal output, .reveal ruby, .reveal section, .reveal summary, .reveal time, .reveal mark, .reveal audio, .reveal video
{
    /* color : #337AB7;*/
    /* color : #134A87; */
    color : #033A77;
}

.hljs {
    background: #000000 !important;
    font-size:20px !important;
}

</style>


