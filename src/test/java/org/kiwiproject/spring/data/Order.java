package org.kiwiproject.spring.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.Date;

/**
 * Spring Data MongoDB domain object for Mongo tests.
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
class Order {

    @Id
    private String id;

    @NonNull
    private String customerId;

    @NonNull
    private double amount;

    @NonNull
    private String status;

    @NonNull
    private Date dateReceived;
}
