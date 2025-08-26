package site.petful.userservice.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class ActorRef {
    @Enumerated(EnumType.STRING)
    @Column(name="type",nullable = false,length = 20)
    private ActorType type;

    @Column(name="id",nullable = false)
    private Long id;

    protected ActorRef() {}
    public ActorRef(ActorType type, Long id) {this.type = type;this.id = id;}

    public ActorType getType() {return type;}
    public Long getId() {return id;}
}
