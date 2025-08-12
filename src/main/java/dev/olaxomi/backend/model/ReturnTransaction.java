package dev.olaxomi.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
}
