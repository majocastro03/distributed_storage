package servidor.aplicacion.util;

import servidor.aplicacion.dto.FileDTO;
import servidor.aplicacion.model.File;
import java.util.List;
import java.util.stream.Collectors;

public class FileConverter {

    public static FileDTO toDTO(File file) {
        if (file == null) {
            return null;
        }

        return new FileDTO(
                file.getId(),
                file.getName(),
                file.getParentId(),
                file.getOwnerId(),
                file.getType(),
                file.getSize(),
                file.getCreatedAt() != null ? file.getCreatedAt().toString() : null,
                file.getUpdatedAt() != null ? file.getUpdatedAt().toString() : null);
    }

    public static File fromDTO(FileDTO dto) {
        if (dto == null) {
            return null;
        }

        File file = new File();
        file.setId(dto.getId());
        file.setName(dto.getName());
        file.setParentId(dto.getParentId());
        file.setOwnerId(dto.getOwnerId());
        file.setType(dto.getType());
        file.setSize(dto.getSize());
        return file;
    }

    public static List<FileDTO> toDTOList(List<File> files) {
        if (files == null) {
            return null;
        }

        return files.stream()
                .map(FileConverter::toDTO)
                .collect(Collectors.toList());
    }
}