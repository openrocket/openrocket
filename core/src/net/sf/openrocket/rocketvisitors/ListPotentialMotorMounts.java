package net.sf.openrocket.rocketvisitors;

import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;

public class ListPotentialMotorMounts extends ListComponents<RocketComponent>
{
   public ListPotentialMotorMounts() {
      super(RocketComponent.class);
   }

   @Override
   protected void doAction(RocketComponent visitable) {
      if (visitable instanceof MotorMount) {
         components.add(visitable);
      }
   }
}
