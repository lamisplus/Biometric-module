package org.lamisplus.modules.biometric.domain;

import lombok.*;
import org.hibernate.annotations.ResultCheckStyle;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "biometric_pims_config")
@SQLDelete(sql = "delete from biometric_pims_config where id = ?", check = ResultCheckStyle.COUNT)
@Where(clause = "archived = 0")
@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder
public class PimsConfig extends BiometricAuditEntity  implements Serializable, Persistable<Long> {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;
	private String  username;
	private String password;
	private String url;
	private Integer archived = 0;
	@Override
	public boolean isNew() {
		return id == null;
	}
}
