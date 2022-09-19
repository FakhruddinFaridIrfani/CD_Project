package com.consolidate.project.repository;

import com.consolidate.project.model.SdnLogger;
import com.consolidate.project.model.SdnNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface SdnNotificationRepository extends JpaRepository<SdnNotification, Integer> {


    @Query(value = "SELECT * FROM ofac.sdn_notification ORDER BY sdn_notification_id DESC LIMIT 10", nativeQuery = true)
    List<SdnNotification> getNotification();
}
