package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.*;

@Entity
@Getter @Setter
public class Category {
    @Id @GeneratedValue
    @Column(name="CATEGORY_ID")
    private Long id;

    private String name;

    @ManyToMany
    @JoinTable(name="category_item",
            joinColumns = @JoinColumn(name="category_id"),
            inverseJoinColumns = @JoinColumn(name="item_id"))
    private List<Item> items = new ArrayList<>();

    //=========================== 셀프 양방향 연관관계 ==========================//
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name="parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> child = new ArrayList<>();

    public void addChildCategory(Category child){
        this.child.add(child);
        child.setParent(this);
    }
    //=========================== 셀프 양방향 연관관계 ==========================//
}
