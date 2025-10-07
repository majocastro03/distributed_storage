package servidor.aplicacion.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

@XmlRootElement(name = "directories")
@jakarta.xml.bind.annotation.XmlAccessorType(jakarta.xml.bind.annotation.XmlAccessType.FIELD)
public class DirectoriesWrapper implements Serializable {
    @XmlElement(name = "directory")
    private List<FileDTO> directories;

    public DirectoriesWrapper() {}

    public List<FileDTO> getDirectories() { return directories; }
    public void setDirectories(List<FileDTO> directories) { this.directories = directories; }
}
