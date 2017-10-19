package tools.file;

import tools.ExceptionHandler;
import tools.IOHelper;

import java.io.FileInputStream;
import java.io.OutputStream;

public class File {

    public String filename;
    public java.io.File content;

    public File(String filename, java.io.File content)throws Exception{
        if(!content.isFile())throw new Exception("Not a File");
        this.filename = filename;
        this.content = content;
    }

    /**
     * Writes the content of this file to the OutputStream
     * @param out
     * @return
     */
    public boolean writeToOutputStream(OutputStream out,boolean printStatus){
        try{
            if(content.canRead()){
                FileInputStream in = new FileInputStream(content);
                boolean b =  IOHelper.writeToOutputStream(in,out,content.length(),printStatus);
                if(b && printStatus) ExceptionHandler.InfoMessage("Written 100 percent");
                in.close();
                return b;
            }
        }catch (Exception e){
            ExceptionHandler.handleException(e, ExceptionHandler.OccClass.FILE);
        }
        return false;


    }
}
