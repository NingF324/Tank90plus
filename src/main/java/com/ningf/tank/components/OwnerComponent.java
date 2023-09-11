package com.ningf.tank.components;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;

public class OwnerComponent extends Component {
    public Entity owner;
    public OwnerComponent(Entity owner) {
        this.owner=owner;
    }
}
