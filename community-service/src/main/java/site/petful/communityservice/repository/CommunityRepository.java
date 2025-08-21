package site.petful.communityservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.petful.communityservice.entity.Post;

@Repository
public interface CommunityRepository extends JpaRepository<Post,Long> {
}
