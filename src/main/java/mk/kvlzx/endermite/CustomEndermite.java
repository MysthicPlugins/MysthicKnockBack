package mk.kvlzx.endermite;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import mk.kvlzx.utils.MessageUtils;
import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.EntityEndermite;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.PathfinderGoal;
import net.minecraft.server.v1_8_R3.PathfinderGoalFloat;
import net.minecraft.server.v1_8_R3.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_8_R3.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_8_R3.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_8_R3.PathfinderGoalTarget;
import net.minecraft.server.v1_8_R3.World;

public class CustomEndermite extends EntityEndermite {
    
    private Player owner;
    private int lifeTime = 600; // 30 segundos
    private int attackCooldown = 0;
    
    public CustomEndermite(World world) {
        super(world);
        this.setSize(0.4F, 0.3F);
        this.goalSelector.a(); // Limpiar los goals default
        this.targetSelector.a(); // Limpiar el selector default
        
        // Setear la inteligencia artificial custom
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.goalSelector.a(3, new PathfinderGoalAttackNearbyPlayers(this, 1.0D, false));
        this.goalSelector.a(4, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.a(5, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        
        // Configurar atributos
        this.getAttributeInstance(GenericAttributes.maxHealth).setValue(8.0D);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.4D);
        
        this.setHealth(8.0F);
    }
    
    public CustomEndermite(World world, Player owner) {
        this(world);
        this.owner = owner;
        this.setCustomName(MessageUtils.getColor("&e" + owner.getName() + "'s Pet &7(30s)"));
        this.setCustomNameVisible(true);
    }
    
    @Override
    public void t_() { // Tick method
        super.t_();
        
        if (this.owner == null || !this.owner.isOnline()) {
            this.die();
            return;
        }
        
        // Actualizar tiempo de vida
        this.lifeTime--;
        if (this.lifeTime <= 0) {
            this.die();
            return;
        }
        
        // Actualizar el nombre con el tiempo restante del endermite
        int secondsLeft = this.lifeTime / 20;
        this.setCustomName(MessageUtils.getColor("&e" + owner.getName() + "'s Pet &7(" + secondsLeft + "s)"));
        
        // Update attack cooldown
        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }
        
        // Hacer que el dueño se suba al endermite
        if (this.owner.getVehicle() == null && this.getBukkitEntity().getLocation().distance(this.owner.getLocation()) < 2) {
            this.getBukkitEntity().setPassenger(this.owner);
        }
    }
    
    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        // No tomar daño desde el dueño
        if (damagesource.getEntity() != null && damagesource.getEntity().getBukkitEntity().equals(this.owner)) {
            return false;
        }
        return super.damageEntity(damagesource, f);
    }
    
    public Player getOwner() {
        return this.owner;
    }
    
    public void setOwner(Player owner) {
        this.owner = owner;
    }
    
    public static class PathfinderGoalFollowOwner extends PathfinderGoal {
        private final CustomEndermite endermite;
        private final double speed;
        private final float maxDistance;
        private final float minDistance;
        private int updateCounter;
        
        public PathfinderGoalFollowOwner(CustomEndermite endermite, double speed, float maxDistance, float minDistance) {
            this.endermite = endermite;
            this.speed = speed;
            this.maxDistance = maxDistance;
            this.minDistance = minDistance;
            this.a(3); // Set mutex bits
        }
        
        @Override
        public boolean a() { // shouldExecute
            Player owner = this.endermite.getOwner();
            if (owner == null || !owner.isOnline()) {
                return false;
            }
            
            Location ownerLoc = owner.getLocation();
            Location endermiteLoc = this.endermite.getBukkitEntity().getLocation();
            
            if (ownerLoc.getWorld() != endermiteLoc.getWorld()) {
                return false;
            }
            
            double distance = ownerLoc.distance(endermiteLoc);
            return distance > this.minDistance;
        }
        
        @Override
        public void c() { // startExecuting
            this.updateCounter = 0;
        }
        
        @Override
        public void d() { // resetTask
            this.endermite.getNavigation().n(); // Stop navigation
        }
        
        @Override
        public void e() { // updateTask
            Player owner = this.endermite.getOwner();
            if (owner == null) return;
            
            this.endermite.getControllerLook().a(owner.getLocation().getX(), 
                owner.getLocation().getY() + owner.getEyeHeight(), 
                owner.getLocation().getZ(), 10.0F, this.endermite.bQ());
            
            if (--this.updateCounter <= 0) {
                this.updateCounter = 10;
                
                Location ownerLoc = owner.getLocation();
                double distance = ownerLoc.distance(this.endermite.getBukkitEntity().getLocation());
                
                if (distance > this.maxDistance) {
                    // Teleport if too far
                    this.endermite.setLocation(ownerLoc.getX(), ownerLoc.getY(), ownerLoc.getZ(), 
                        ownerLoc.getYaw(), ownerLoc.getPitch());
                } else if (distance > this.minDistance) {
                    // Seguir al dueño
                    this.endermite.getNavigation().a(ownerLoc.getX(), ownerLoc.getY(), ownerLoc.getZ(), this.speed);
                }
            }
        }
    }
    
    public static class PathfinderGoalAttackNearbyPlayers extends PathfinderGoalTarget {
        private final CustomEndermite endermite;
        
        public PathfinderGoalAttackNearbyPlayers(CustomEndermite endermite, double speed, boolean checkSight) {
            super(endermite, checkSight, false);
            this.endermite = endermite;
        }
        
        @Override
        public boolean a() { // shouldExecute
            Player owner = this.endermite.getOwner();
            if (owner == null) return false;
            
            // Encontrar jugadores cerca del endermite para atacar
            for (Object obj : this.endermite.world.players) {
                if (obj instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) obj;
                    Player bukkitPlayer = player.getBukkitEntity();
                    
                    // No atacar al dueño
                    if (bukkitPlayer.equals(owner)) continue;
                    
                    double distance = this.endermite.h(player); // getDistanceSq
                    if (distance < 25.0D) { // 5 blocks
                        this.endermite.setGoalTarget(player);
                        return true;
                    }
                }
            }
            return false;
        }
        
        @Override
        public void c() { // startExecuting
            super.c();
        }
    }
}
