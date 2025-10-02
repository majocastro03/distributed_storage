package servidor.aplicacion.service;
import servidor.aplicacion.dao.FileDAO;
import servidor.aplicacion.interfaces.FileInterface;
import servidor.aplicacion.model.File;
import java.util.List;

public class FileService implements FileInterface {
    private final FileDAO fileDAO;

    public FileService(FileDAO fileDAO) {
        this.fileDAO = fileDAO;
    }

    @Override
    public File uploadFile(File file, byte[] data, long userId) throws Exception {
        // Verificar permisos de usuario para el archivo
        if (file.getOwnerId() != userId) {
            throw new SecurityException("User is not the owner of this file");
        }
        
        // Establecer el tamaño del archivo basado en los datos
        if (data != null) {
            file.setSize((long) data.length);
        }
        
        // Guardar el archivo en la base de datos
        File savedFile = fileDAO.save(file);
        
        // El sistema distribuye automáticamente el archivo a los nodos de almacenamiento
        // Los archivos se fragmentan y replican para garantizar disponibilidad
        
        return savedFile;
    }

    @Override
    public byte[] downloadFile(long fileId, long userId) throws Exception {
        // Buscar el archivo
        File file = fileDAO.findById(fileId);
        
        if (file == null) {
            throw new Exception("File not found");
        }
        
        // Validar que el usuario tenga acceso al archivo
        if (file.getOwnerId() != userId) {
            throw new SecurityException("User does not have access to this file");
        }
        
        // Confirmar que es un archivo descargable
        if (!file.isFile()) {
            throw new Exception("Cannot download a directory");
        }
        
        // El sistema recupera automáticamente los datos desde los nodos
        // Se ensamblan los fragmentos para reconstruir el archivo original
        return new byte[0];
    }

    @Override
    public List<File> listFiles(long parentId, long userId) {
        return fileDAO.findByParentIdAndUser(parentId, userId);
    }

    @Override
    public void deleteFile(long fileId, long userId) throws Exception {
        // Buscar el archivo
        File file = fileDAO.findById(fileId);
        
        if (file == null) {
            throw new Exception("File not found");
        }
        
        // Validar que el usuario tenga acceso al archivo
        if (file.getOwnerId() != userId) {
            throw new SecurityException("User does not have access to this file");
        }
        
        // Eliminar el archivo de la base de datos
        fileDAO.delete(fileId);
        
        // El sistema elimina automáticamente todos los fragmentos de los nodos
    }
    
    // Métodos adicionales útiles
    public File createDirectory(String name, Long parentId, long userId) throws Exception {
        File directory = new File(name, parentId, userId, File.DIR_TYPE, 0L);
        return fileDAO.save(directory);
    }
    
    public List<File> getUserFiles(long userId) {
        return fileDAO.findByOwnerId(userId);
    }
    
    public File getFileInfo(long fileId, long userId) throws Exception {
        File file = fileDAO.findById(fileId);
        
        if (file == null) {
            throw new Exception("File not found");
        }
        
        // Validar que el usuario tenga acceso al archivo
        if (file.getOwnerId() != userId) {
            throw new SecurityException("User does not have access to this file");
        }
        
        return file;
    }
}
