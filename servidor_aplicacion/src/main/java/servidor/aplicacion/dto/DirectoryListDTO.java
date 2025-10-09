package servidor.aplicacion.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import java.io.Serializable;
import java.util.List;

@XmlRootElement(name = "directories")
@XmlAccessorType(XmlAccessType.FIELD)
public class DirectoryListDTO implements Serializable {
    @XmlElement(name = "directory")
    private List<FileDTO> directories;

    public DirectoryListDTO() {
        this.directories = new java.util.ArrayList<>();
    }

    public List<FileDTO> getDirectories() {
        return directories;
    }

    public void setDirectories(List<FileDTO> directories) {
        this.directories = directories;
    }
}
