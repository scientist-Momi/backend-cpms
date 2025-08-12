package dev.olaxomi.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
//@ToString(exclude = "transactionDetails")
@Data
@Entity
@Table(name = "return_transactions")
public class ReturnTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "return_id")
    private Long returnId;

    @ManyToOne
    private CustomerTransaction transaction;
}
