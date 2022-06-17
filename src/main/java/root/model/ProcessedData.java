package root.model;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "processed_data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedData {
    @Id
    private String id;
    @NotNull
    private String data;
    @NotNull
    private Instant processingTimestamp;
}
