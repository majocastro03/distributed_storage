package servidor.aplicacion.util;

import servidor.aplicacion.dto.FileChunkDTO;
import servidor.aplicacion.model.FileChunk;
import servidor.aplicacion.model.Node;

import java.util.ArrayList;
import java.util.List;

public class FileChunkConverter {

    // Convierte FileChunk entity a DTO

    public static FileChunkDTO toDTO(FileChunk chunk) {
        if (chunk == null) {
            return null;
        }

        FileChunkDTO dto = new FileChunkDTO();
        dto.setId(chunk.getId());
        dto.setFileId(chunk.getFileId());
        dto.setNodeId(chunk.getNodeId());
        dto.setChunkIndex(chunk.getChunkIndex());
        dto.setChecksum(chunk.getChecksum());
        dto.setReplicated(chunk.getReplicated());
        dto.setCreatedAt(chunk.getCreatedAt());

        return dto;
    }

    // Convierte FileChunk entity a DTO con información del nodo

    public static FileChunkDTO toDTO(FileChunk chunk, Node node) {
        FileChunkDTO dto = toDTO(chunk);
        if (dto != null && node != null) {
            dto.setNodeInfo(node.getIp() + ":" + node.getPort());
        }
        return dto;
    }

    // Convierte DTO a FileChunk entity

    public static FileChunk toEntity(FileChunkDTO dto) {
        if (dto == null) {
            return null;
        }

        FileChunk chunk = new FileChunk();
        chunk.setId(dto.getId());
        chunk.setFileId(dto.getFileId());
        chunk.setNodeId(dto.getNodeId());
        chunk.setChunkIndex(dto.getChunkIndex());
        chunk.setChecksum(dto.getChecksum());
        chunk.setReplicated(dto.getReplicated());
        chunk.setCreatedAt(dto.getCreatedAt());

        return chunk;
    }

    // Convierte lista de FileChunk entities a DTOs

    public static List<FileChunkDTO> toDTOList(List<FileChunk> chunks) {
        if (chunks == null) {
            return new ArrayList<>();
        }

        List<FileChunkDTO> dtos = new ArrayList<>();
        for (FileChunk chunk : chunks) {
            FileChunkDTO dto = toDTO(chunk);
            if (dto != null) {
                dtos.add(dto);
            }
        }

        return dtos;
    }

    // Convierte lista de DTOs a FileChunk entities

    public static List<FileChunk> toEntityList(List<FileChunkDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }

        List<FileChunk> chunks = new ArrayList<>();
        for (FileChunkDTO dto : dtos) {
            FileChunk chunk = toEntity(dto);
            if (chunk != null) {
                chunks.add(chunk);
            }
        }

        return chunks;
    }

    // Crea un resumen de distribución de chunks

    public static String createDistributionSummary(List<FileChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return "No hay chunks distribuidos";
        }

        StringBuilder summary = new StringBuilder();
        summary.append("Distribución de ").append(chunks.size()).append(" chunks:\n");

        // Agrupar por nodo
        chunks.stream()
                .collect(java.util.stream.Collectors.groupingBy(FileChunk::getNodeId))
                .forEach((nodeId, nodeChunks) -> {
                    long replicatedCount = nodeChunks.stream()
                            .filter(FileChunk::isReplicated)
                            .count();
                    summary.append("Nodo ").append(nodeId).append(": ")
                            .append(nodeChunks.size()).append(" chunks (")
                            .append(replicatedCount).append(" replicados)\n");
                });

        return summary.toString();
    }

    // Valida la integridad de un chunk DTO

    public static boolean isValidChunkDTO(FileChunkDTO dto) {
        return dto != null &&
                dto.getFileId() != null &&
                dto.getNodeId() != null &&
                dto.getChunkIndex() != null &&
                dto.getChecksum() != null &&
                !dto.getChecksum().trim().isEmpty();
    }

    // Crea un chunk DTO básico para testing

    public static FileChunkDTO createTestChunkDTO(Long fileId, Long nodeId, Integer chunkIndex) {
        FileChunkDTO dto = new FileChunkDTO();
        dto.setFileId(fileId);
        dto.setNodeId(nodeId);
        dto.setChunkIndex(chunkIndex);
        dto.setChecksum("test_checksum_" + fileId + "_" + chunkIndex);
        dto.setReplicated(false);
        dto.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));

        return dto;
    }
}