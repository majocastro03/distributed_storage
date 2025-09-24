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
        // Validar que el archivo pertenezca al usuario
        if (file.getOwnerId() != userId) {
            throw new SecurityException("User is not the owner of this file");
        }
        
        // Establecer el tamaño del archivo basado en los datos
        if (data != null) {
            file.setSize((long) data.length);
        }
        
        // Guardar el archivo en la base de datos
        File savedFile = fileDAO.save(file);
        
        // Aquí iría la lógica de redundancia y coordinación con nodos
        // Por ejemplo, dividir el archivo en chunks y distribuirlos
        // TODO: Implementar distribución a nodos de almacenamiento
        
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
        
        // Validar que sea un archivo y no un directorio
        if (!file.isFile()) {
            throw new Exception("Cannot download a directory");
        }
        
        // Aquí iría la coordinación con nodos para obtener los datos
        // TODO: Implementar descarga desde nodos de almacenamiento
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
        
        // Aquí iría la lógica para eliminar los chunks de los nodos
        // TODO: Implementar eliminación de chunks en nodos de almacenamiento
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
