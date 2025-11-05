package com.lts5.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.primes.library.entity.BaseEntity;
import com.primes.library.entity.SnowflakeId;
import com.primes.library.entity.SnowflakeIdEntityListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.List;

@Entity
@EntityListeners(SnowflakeIdEntityListener.class)
@Table(name = "code_groups")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CodeGroup extends BaseEntity {

    @Id
    @SnowflakeId
    @Column(name = "code_group_id")
    private Long id;

    @Builder.Default
    @Column(name = "is_delete")
    private Boolean isDelete = false;

    @Builder.Default
    @Column(name = "is_use")
    private Boolean isUse = true;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "is_root", nullable = false)
    private Boolean isRoot;

    @Column(name = "group_code", length = 3, nullable = false)
    private String groupCode;

    @Column(name = "group_name", length = 100, nullable = false)
    private String groupName;

    @Column(name = "description", length = 255)
    private String description;

    @JsonIgnore
    @OneToMany(mappedBy = "codeGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Code> codes;
}
