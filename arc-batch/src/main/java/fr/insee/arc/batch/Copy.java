package fr.insee.arc.batch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;


public class Copy {

	public static void main(String[] args) {
		Path sourcePath = Paths.get(args[0]);
		Path destPath = Paths.get(args[1]);
		
		String[] compareList;
		
		if (args.length>2)
		{
			Path comparePath = Paths.get(args[2]);
			File compareFile=new File(comparePath.toAbsolutePath().toString());
			compareList=compareFile.list();
		}
		else
		{
			compareList=new String[0];
		}

		
		File sourceFile=new File(sourcePath.toAbsolutePath().toString());
		File destFile=new File(destPath.toAbsolutePath().toString());
		
		ArrayList<String> destList=new ArrayList<String>(Arrays.asList(destFile.list()));

		
		for (String f : sourceFile.list()) {
			boolean notFound=true;
			
			if (destList.contains(f))
			{
				notFound=false;
			}
			
			if (notFound)
			{
				for (String fc : compareList) {
					if (fc.startsWith(f))
					{
						notFound=false;
						break;
					}
				}
			}
			
			if (notFound)
			{
				try {
					Files.copy(new File(sourceFile, f).toPath(), new File(destFile, f).toPath());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	    }
		
		
//		try {
//		System.out.println(args[0]+" > "+args[1] );	
//			
//		Path sourcePath = Paths.get(args[0]);
//		Path destPath = Paths.get(args[1]);
//		
//		File sourceFile=new File(sourcePath.toAbsolutePath().toString());
//		File destFile=new File(destPath.toAbsolutePath().toString());
//		
//		if (sourceFile.isFile())
//		{
//			if (destFile.exists())
//			{
//				FileUtils.forceDelete(destFile);
//			}
//				Files.copy(sourcePath, destPath);
//		}
//		
//		if (sourceFile.isDirectory())
//		{
//			if (!destFile.exists())
//			{
//				destFile.mkdir();
//			}
//			else
//			{
//				FileUtils.forceDelete(destFile);
//			}
//			
//		    for (String f : sourceFile.list()) {
//		        Files.copy(new File(sourceFile, f).toPath(), new File(destFile, f).toPath());
//		    }
//		    
//			
//		}
//
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}
