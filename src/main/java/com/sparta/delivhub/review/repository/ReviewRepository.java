package com.sparta.delivhub.review.repository;

import com.sparta.delivhub.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
}
