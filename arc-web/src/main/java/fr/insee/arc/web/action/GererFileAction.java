package fr.insee.arc.web.action;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.h2.store.fs.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;
import fr.insee.arc.web.util.VObject;
import lombok.Getter;
import lombok.Setter;

@Component
@Results({ @Result(name = "success", location = "/jsp/gererFile.jsp"), @Result(name = "index", location = "/jsp/index.jsp") })
@Getter
@Setter
public class GererFileAction implements SessionAware, IConstanteCaractere {
    @Override
    public void setSession(Map<String, Object> session) {
        this.viewDirIn.setMessage("");
        this.viewDirOut.setMessage("");
    }
    
    @Autowired
    public PropertiesHandler PROPERTIES;


    public String dirIn ;
    public String dirOut ;

    public static String REPERTOIRE_EFFACABLE="TO_DELETE";

    private static final Logger logger = Logger.getLogger(GererFileAction.class);
    @Autowired
    @Qualifier("viewDirIn")
    VObject viewDirIn;

    @Autowired
    @Qualifier("viewDirOut")
    VObject viewDirOut;

    // pour charger un fichier CSV
        private String scope;

    public String sessionSyncronize() {
        this.viewDirIn.setActivation(this.scope);
        this.viewDirOut.setActivation(this.scope);
        Boolean defaultWhenNoScope = true;

        if (this.viewDirIn.getIsScoped()) {
            initializeDirIn();
            defaultWhenNoScope = false;
        }

        if (this.viewDirOut.getIsScoped()) {
            initializeDirOut();
            defaultWhenNoScope = false;
        }


        if (defaultWhenNoScope) {
            System.out.println("default");

            initializeDirIn();
            this.viewDirIn.setIsActive(true);
            this.viewDirIn.setIsScoped(true);

            initializeDirOut();
            this.viewDirOut.setIsActive(true);
            this.viewDirOut.setIsScoped(true);
        }
        return "success";

    }

    // private SessionMap session;
    // visual des Files
    public void initializeDirIn() {
        System.out.println("/* initializeDirIn */");
        HashMap<String, String> defaultInputFields = new HashMap<String, String>();


        if (this.dirIn==null)
        {
            this.dirIn=PROPERTIES.getRootDirectory();
        }

        ArrayList<ArrayList<String>> listeFichier = getFilesFromDirectory(this.dirIn, this.viewDirIn.mapFilterFields());

        this.viewDirIn.initializeByList(listeFichier, defaultInputFields);

    }


    @Action(value = "/selectFile")
    public String selectFile() {
        System.out.println("selectFile " + this.scope);
        return sessionSyncronize();
    }

    @Action(value = "/selectDirIn")
    public String selectDirIn() {
        System.out.println("selectDirIn " + this.scope);
        return sessionSyncronize();
    }

    @Action(value = "/seeDirIn")
    public String seeDirIn() {
        System.out.println("seeDirIn " + this.scope);

        Map<String,ArrayList<String>> m=this.viewDirIn.mapContentSelected();
        if (!m.isEmpty())
        {
            if(m.get("isdirectory").get(0).equals("true"))
            {
                this.dirIn=this.dirIn+m.get("filename").get(0)+File.separator;
            }
        }


        return sessionSyncronize();
    }

    @Action(value = "/sortDirIn")
    public String sortDirIn() {
        this.viewDirIn.sort();
        return sessionSyncronize();
    }

    @Action(value = "/transferDirIn")
    public String transferDirIn() {
          Map<String,ArrayList<String>> m=this.viewDirIn.mapContentSelected();
          if (!m.isEmpty())
          {
              for (String f:m.get("filename"))
              {
                File fileIn = new File(this.dirIn + f);
                File fileOut = new File(this.dirOut + f);
                fileIn.renameTo(fileOut);
              }
          }
          else
          {
              m=this.viewDirIn.mapContent();
              while (!m.isEmpty())
              {
                  for (int i=0;i<m.get("filename").size();i++)
                  {
                      if (m.get("isdirectory").get(i).equals("false"))
                      {
                        File fileIn = new File(this.dirIn + m.get("filename").get(i));
                        File fileOut = new File(this.dirOut + m.get("filename").get(i));
                        fileIn.renameTo(fileOut);
                      }
                  }
                  ArrayList<ArrayList<String>> listeFichier = getFilesFromDirectory(this.dirIn, this.viewDirIn.mapFilterFields());
                  this.viewDirIn.initializeByList(listeFichier, new HashMap<String, String>());
                  m=this.viewDirIn.mapContent();
              }
          }
        return sessionSyncronize();
    }

    @Action(value = "/copyDirIn")
    public String copyDirIn() {
          Map<String,ArrayList<String>> m=this.viewDirIn.mapContentSelected();
          if (!m.isEmpty())
          {
              for (String f:m.get("filename"))
              {
                File fileIn = new File(this.dirIn + f);
                File fileOut = new File(this.dirOut + f);
                    try {
                        Files.copy(fileIn.toPath(), fileOut.toPath());
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
              }
          }
          else
          {
              m=this.viewDirIn.mapContent();
              while (!m.isEmpty())
              {
                  for (int i=0;i<m.get("filename").size();i++)
                  {
                      if (m.get("isdirectory").get(i).equals("false"))
                      {
                        File fileIn = new File(this.dirIn + m.get("filename").get(i));
                        File fileOut = new File(this.dirOut + m.get("filename").get(i));
                            try {
                                Files.copy(fileIn.toPath(), fileOut.toPath());
                            } catch (IOException e) {
                            }
                        }
                  }
                  ArrayList<ArrayList<String>> listeFichier = getFilesFromDirectory(this.dirIn, this.viewDirIn.mapFilterFields());
                  this.viewDirIn.initializeByList(listeFichier, new HashMap<String, String>());
                  m=this.viewDirIn.mapContent();
              }
          }
        return sessionSyncronize();
    }

    @Action(value = "/renameIn")
    public String renameIn() {
          Map<String,ArrayList<String>> m=this.viewDirIn.mapSameContentFromPreviousVObject();
          Map<String,ArrayList<String>> n=this.viewDirIn.mapSameContentFromPreviousVObject();

          if (!m.isEmpty())
          {
              for (int i=0; i<m.get("filename").size();i++)
              {
                File fileIn = new File(this.dirIn + m.get("filename").get(i));
                File fileOut = new File(this.dirIn + n.get("filename").get(i));
                fileIn.renameTo(fileOut);
              }
          }
        return sessionSyncronize();
    }


    @Action(value = "/addDirIn")
    public String addDirIn() {
      HashMap<String,ArrayList<String>> m=this.viewDirIn.mapInputFields();
     if (!m.isEmpty())
     {
         if (m.get("filename").get(0)!=null && !m.get("filename").get(0).trim().equals(""))
         {
             FileUtils.createDirectory(this.dirIn+m.get("filename").get(0).trim());
         }
     }
        return sessionSyncronize();
    }

    @Action(value = "/delDirIn")
    public String delDirIn() {


        if (!this.dirIn.contains(REPERTOIRE_EFFACABLE))
        {
            File f=new File(this.dirIn);
            if (f.listFiles().length==0)
            {
            FileUtils.delete(this.dirIn);
            this.dirIn=null;
            }
            else
            {
                this.viewDirIn.setMessage("Le répertoire doit être vide ou contenir "+REPERTOIRE_EFFACABLE+" pour etre effaçable. ");
            }
        }
        else
        {
            FileUtils.deleteRecursive(this.dirIn, true);
            this.dirIn=null;
        }
        return sessionSyncronize();
    }


    public VObject getViewDirIn() {
        return this.viewDirIn;
    }

    public void setViewDirIn(VObject viewDirIn) {
        this.viewDirIn = viewDirIn;
    }



    // private SessionMap session;
    // visual des Files
    public void initializeDirOut() {
        System.out.println("/* initializeDirOut */");
        HashMap<String, String> defaultInputFields = new HashMap<String, String>();


        if (this.dirOut==null)
        {
            this.dirIn=PROPERTIES.getRootDirectory();
        }

        ArrayList<ArrayList<String>> listeFichier = getFilesFromDirectory(this.dirOut, this.viewDirOut.mapFilterFields());

        this.viewDirOut.initializeByList(listeFichier, defaultInputFields);

    }

    @Action(value = "/selectDirOut")
    public String selectDirOut() {
        System.out.println("selectDirOut " + this.scope);
        return sessionSyncronize();
    }

    @Action(value = "/seeDirOut")
    public String seeDirOut() {
        System.out.println("seeDirOut " + this.scope);

        Map<String,ArrayList<String>> m=this.viewDirOut.mapContentSelected();

//        System.out.println(m);
        if (!m.isEmpty())
        {
            if(m.get("isdirectory").get(0).equals("true"))
            {
                this.dirOut=this.dirOut+m.get("filename").get(0)+File.separator;
            }
        }


        return sessionSyncronize();
    }

    @Action(value = "/sortDirOut")
    public String sortDirOut() {
        this.viewDirOut.sort();
        return sessionSyncronize();
    }

    @Action(value = "/transferDirOut")
    public String transferDirOut() {
          Map<String,ArrayList<String>> m=this.viewDirOut.mapContentSelected();
          if (!m.isEmpty())
          {
              for (String f:m.get("filename"))
              {
                File fileIn = new File(this.dirOut + f);
                File fileOut = new File(this.dirIn + f);
                fileIn.renameTo(fileOut);
              }
          }
          else
          {

              m=this.viewDirOut.mapContent();
              while (!m.isEmpty())
              {
                  for (int i=0;i<m.get("filename").size();i++)
                  {
                      if (m.get("isdirectory").get(i).equals("false"))
                      {
                        File fileIn = new File(this.dirOut + m.get("filename").get(i));
                        File fileOut = new File(this.dirIn + m.get("filename").get(i));
                        fileIn.renameTo(fileOut);
                      }
                  }
                  ArrayList<ArrayList<String>> listeFichier = getFilesFromDirectory(this.dirOut, this.viewDirOut.mapFilterFields());
                  this.viewDirOut.initializeByList(listeFichier, new HashMap<String, String>());
                  m=this.viewDirOut.mapContent();
              }
          }
        return sessionSyncronize();
    }


    @Action(value = "/copyDirOut")
    public String copyDirOut() {
          Map<String,ArrayList<String>> m=this.viewDirOut.mapContentSelected();
          if (!m.isEmpty())
          {
              for (String f:m.get("filename"))
              {
                File fileIn = new File(this.dirOut + f);
                File fileOut = new File(this.dirIn + f);
                try {
                    Files.copy(fileIn.toPath(), fileOut.toPath());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
              }
          }
          else
          {

              m=this.viewDirOut.mapContent();
              while (!m.isEmpty())
              {
                  for (int i=0;i<m.get("filename").size();i++)
                  {
                    if (m.get("isdirectory").get(i).equals("false"))
                    {
                        File fileIn = new File(this.dirOut + m.get("filename").get(i));
                        File fileOut = new File(this.dirIn + m.get("filename").get(i));
                            try {
                                Files.copy(fileIn.toPath(), fileOut.toPath());
                            } catch (IOException e) {}
                    }
                  }
                  ArrayList<ArrayList<String>> listeFichier = getFilesFromDirectory(this.dirOut, this.viewDirOut.mapFilterFields());
                  this.viewDirOut.initializeByList(listeFichier, new HashMap<String, String>());
                  m=this.viewDirOut.mapContent();
              }
          }
        return sessionSyncronize();
    }


    @Action(value = "/renameOut")
    public String renameOut() {
          Map<String,ArrayList<String>> m=this.viewDirOut.mapSameContentFromPreviousVObject();
          Map<String,ArrayList<String>> n=this.viewDirOut.mapSameContentFromPreviousVObject();

          if (!m.isEmpty())
          {
              for (int i=0; i<m.get("filename").size();i++)
              {
                File fileIn = new File(this.dirOut + m.get("filename").get(i));
                File fileOut = new File(this.dirOut + n.get("filename").get(i));
                fileIn.renameTo(fileOut);
              }
          }
        return sessionSyncronize();
    }


    @Action(value = "/addDirOut")
    public String addDirOut() {
      HashMap<String,ArrayList<String>> m=this.viewDirOut.mapInputFields();
     if (!m.isEmpty())
     {
         if (m.get("filename").get(0)!=null && !m.get("filename").get(0).trim().equals(""))
         {
             FileUtils.createDirectory(this.dirOut+m.get("filename").get(0).trim());
         }
     }


        return sessionSyncronize();
    }

    @Action(value = "/delDirOut")
    public String delDirOut() {


        if (!this.dirOut.contains(REPERTOIRE_EFFACABLE))
        {
            File f=new File(this.dirOut);
            if (f.listFiles().length==0)
            {
            FileUtils.delete(this.dirOut);
            this.dirOut=null;
            }
            else
            {
                this.viewDirOut.setMessage("Le répertoire doit être vide ou contenir "+REPERTOIRE_EFFACABLE+" pour etre effaçable. ");
            }
        }
        else
        {
            FileUtils.deleteRecursive(this.dirOut, true);
            this.dirOut=null;
        }
        return sessionSyncronize();
    }


    public ArrayList<ArrayList<String>> getFilesFromDirectory(String dir, HashMap<String,ArrayList<String>> filter2)
    {
        HashMap<String,ArrayList<String>> filter=filter2;
        ArrayList<ArrayList<String>> result=new ArrayList<ArrayList<String>>();

        ArrayList<String> entete = new ArrayList<String>();
        entete.add("filename");
        entete.add("isdirectory");
        result.add(entete);

        ArrayList<String> format = new ArrayList<String>();
        format.add("text");
        format.add("text");
        result.add(format);


//      if (dir.substring(dir.length()-1, dir.length()).equals("\\"))
//      {
//          System.out.println("yoooo");
//          dir=dir.substring(0,dir.length()-1);
//      }
//
        File files=new File(dir);
//      System.out.println(dir);
        int nb=0;


        // java de merde...
        if (filter==null)
        {
            filter=new  HashMap<String,ArrayList<String>>();
        }

        if (filter.isEmpty())
        {
            filter.put("filename", new ArrayList<String>());
            filter.put("isdirectory", new ArrayList<String>());
        }

        if (filter.get("filename")==null)
        {
            filter.put("filename", new ArrayList<String>());
        }

        if (filter.get("isdirectory")==null)
        {
            filter.put("isdirectory", new ArrayList<String>());
        }


        if (filter.get("filename").size()==0)
            {
                filter.get("filename").add("");
            }

        if (filter.get("isdirectory").size()==0)
            {
                filter.get("isdirectory").add("");
            }

        if (filter.get("filename").get(0)==null)
        {
            filter.get("filename").set(0, "");
        }

        if (filter.get("isdirectory").get(0)==null)
        {
            filter.get("isdirectory").set(0, "");
        }

        System.out.println(filter);


        for (File f:files.listFiles())
        {
            boolean toInsert=true;
            if (!filter.get("filename").get(0).equals("") && !f.getName().contains(filter.get("filename").get(0)))
                    {
                        toInsert=false;
                    }

            if (!filter.get("isdirectory").get(0).equals("") && "true".startsWith(filter.get("isdirectory").get(0)) && !f.isDirectory())
            {
                toInsert=false;
            }

            if (!filter.get("isdirectory").get(0).equals("") && "false".startsWith(filter.get("isdirectory").get(0)) && f.isDirectory())
            {
                toInsert=false;
            }

            if (toInsert)
            {
                ArrayList<String> fileAttribute = new ArrayList<String>();
                fileAttribute.add(f.getName());
                fileAttribute.add(""+f.isDirectory());
                result.add(fileAttribute);
                nb++;
                if (nb>50)
                {
                    break;
                }
            }
        }



        return result;

    }


}