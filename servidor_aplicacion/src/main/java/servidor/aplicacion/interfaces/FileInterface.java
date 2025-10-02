package servidor.aplicacion.interfaces;

import servidor.aplicacion.model.File;
import java.util.List;

public interface FileInterface {
    public File uploadFile(File file, byte[] data, long userId) throws Exception;

    public byte[] downloadFile(long fileId, long userId) throws Exception;

    public List<File> listFiles(long parentId, long userId);

    public void deleteFile(long fileId, long userId) throws Exception;
}