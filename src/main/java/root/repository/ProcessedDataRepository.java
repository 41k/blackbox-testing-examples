package root.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import root.model.ProcessedData;

public interface ProcessedDataRepository extends JpaRepository<ProcessedData, String> {
}
