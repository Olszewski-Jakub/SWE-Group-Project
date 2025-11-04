package ie.universityofgalway.groupnine.infrastructure.auth.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OAuthAccountJpaRepository extends JpaRepository<OAuthAccountEntity, UUID> {

    @org.springframework.data.jpa.repository.Query(value = "select exists(select 1 from oauth_accounts where provider = CAST(:provider as oauth_provider) and user_id = :userId)", nativeQuery = true)
    boolean existsByProviderAndUserId(@org.springframework.data.repository.query.Param("provider") String provider,
                                      @org.springframework.data.repository.query.Param("userId") UUID userId);

    @org.springframework.data.jpa.repository.Query(value = "select * from oauth_accounts where provider = CAST(:provider as oauth_provider) and provider_user_id = :providerUserId limit 1", nativeQuery = true)
    OAuthAccountEntity findByProviderAndProviderUserId(@org.springframework.data.repository.query.Param("provider") String provider,
                                                       @org.springframework.data.repository.query.Param("providerUserId") String providerUserId);
}
