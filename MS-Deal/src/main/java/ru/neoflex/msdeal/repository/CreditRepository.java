package ru.neoflex.msdeal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.neoflex.msdeal.model.CreditEntity;

import java.util.UUID;

@Repository
public interface CreditRepository extends JpaRepository<CreditEntity, UUID> {
}
