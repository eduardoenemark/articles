package br.com.eduardoenemark.pjrw.app.server.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "tb_product")
@Entity(name = "pjrw.app.server.entity.Product")
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_product_gen")
    @SequenceGenerator(name = "sq_product_gen", sequenceName = "sq_product", allocationSize = 1)
    @Column(name = "id", precision = 10)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "producer")
    private String producer;

    @Column(name = "universal_product_code")
    private String universalProductCode;

    @Column(name = "country")
    private String country;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "entry_date")
    private LocalDate entryDate;

    @Column(name = "amount", precision = 10)
    private Integer amount;
}