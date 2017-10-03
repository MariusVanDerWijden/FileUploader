package tools.file;

import tools.ExceptionHandler;

import java.io.File;
import java.util.ArrayList;

public class FileFinder {

    public ArrayList<tools.file.File> files;
    public String directory;

    public FileFinder(String directory){
        this.directory = directory;
        File dir = new File(directory);
        if(!dir.isFile() && dir.listFiles()==null)
            files = null;
        else {
            files = new ArrayList<>();
            crawlDirectoryRecursively(dir, "");
        }
    }

    /**
     * Crawls a Directory recursively, adds all found files to the ArrayList
     * removes spaces from all filenames
     * @param dir
     * @param baseDir
     */
    private void crawlDirectoryRecursively(File dir, String baseDir){
        if(dir == null || baseDir == null) return;
        try {
            String newBaseDir = (baseDir +"/"+ dir.getName()).replaceAll(" ","");
            if (dir.isFile())
                files.add(new tools.file.File(newBaseDir, dir));
            else
                for(File f: dir.listFiles()){
                    crawlDirectoryRecursively(f,newBaseDir);
                }
        }catch (Exception e){
            ExceptionHandler.handleException(e, ExceptionHandler.OccClass.FILE_FINDER);
        }
    }

    /**
     * Returns a list of files to be send, filtered by list
     * (only files not in list should be send)
     * @param list
     * @return
     */
    public ArrayList<tools.file.File> compileListOfFiles(ArrayList<String> list){
        if(list == null) return files;
        ArrayList<tools.file.File> newFiles = new ArrayList<>();
        for(tools.file.File f : files){
            if(!list.contains(f.filename))
                newFiles.add(f);
        }
        return newFiles;
    }

}
