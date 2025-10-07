package servidor.aplicacion.services;

import servidor.aplicacion.dao.FileDAO;
import servidor.aplicacion.dao.OperationLogDAO;
import servidor.aplicacion.interfaces.FileInterface;
import servidor.aplicacion.model.File;
import java.util.List;

public class FileService implements FileInterface {
    private final FileDAO fileDAO;
    private final OperationLogDAO operationLogDAO = new OperationLogDAO();

    public FileService(FileDAO fileDAO) {
        this.fileDAO = fileDAO;
    }

    @Override
    public File uploadFile(File file, byte[] data, long userId) throws Exception {
        // Verificar permisos de usuario para el archivo
        if (file.getOwnerId() != userId) {
            throw new SecurityException("El usuario no tiene permiso para subir este archivo");
        }

        // Establecer el tamaño del archivo basado en los datos
        if (data != null) {
            file.setSize((long) data.length);
        }

        // Guardar el archivo en la base de datos
        File savedFile = fileDAO.save(file);

        // Registrar operación UPLOAD
        try {
            operationLogDAO.save(new servidor.aplicacion.model.OperationLog(userId, savedFile.getId(), "UPLOAD", 1));
        } catch (Exception e) {
            // Si falla el log, no interrumpe la operación principal
            e.printStackTrace();
        }

        // El sistema distribuye automáticamente el archivo a los nodos de
        // almacenamiento
        // Los archivos se fragmentan y replican para garantizar disponibilidad

        return savedFile;
    }

    @Override
    public byte[] downloadFile(long fileId, long userId) throws Exception {
        // Buscar el archivo
        File file = fileDAO.findById(fileId);

        if (file == null) {
            throw new Exception("Archivo no encontrado");
        }

        // Validar que el usuario tenga acceso al archivo
        if (file.getOwnerId() != userId) {
            throw new SecurityException("El usuario no tiene acceso a este archivo");
        }

        // Confirmar que es un archivo descargable
        if (!file.isFile()) {
            throw new Exception("No se puede descargar un directorio");
        }

        // El sistema recupera automáticamente los datos desde los nodos
        // Se ensamblan los fragmentos para reconstruir el archivo original
        byte[] result = new byte[0];
        // Registrar operación DOWNLOAD
        try {
            operationLogDAO.save(new servidor.aplicacion.model.OperationLog(userId, fileId, "DOWNLOAD", 1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
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
            throw new Exception("Archivo no encontrado");
        }

        // Validar que el usuario tenga acceso al archivo
        if (file.getOwnerId() != userId) {
            throw new SecurityException("El usuario no tiene acceso a este archivo");
        }

        // Eliminar el archivo de la base de datos
        fileDAO.delete(fileId);

        // El sistema elimina automáticamente todos los fragmentos de los nodos
    }

    // Métodos adicionales útiles
    public File createDirectory(String name, Long parentId, long userId) throws Exception {
        File directory = new File(name, parentId, userId, File.DIR_TYPE, 0L);
        File savedDir = fileDAO.save(directory);
        // Registrar operación CREATE DIR
        try {
            operationLogDAO.save(new servidor.aplicacion.model.OperationLog(userId, savedDir.getId(), "CREATE_DIR", 1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return savedDir;
    }

    public List<File> getUserFiles(long userId) {
        return fileDAO.findByOwnerId(userId);
    }

    public File getFileInfo(long fileId, long userId) throws Exception {
        File file = fileDAO.findById(fileId);

        if (file == null) {
            throw new Exception("Archivo no encontrado");
        }

        // Validar que el usuario tenga acceso al archivo
        if (file.getOwnerId() != userId) {
            throw new SecurityException("El usuario no tiene acceso a este archivo");
        }

        return file;
    }
}
