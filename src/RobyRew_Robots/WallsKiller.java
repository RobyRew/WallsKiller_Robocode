package RobyRew_Robots;
import robocode.*;
import java.awt.Color;
import robocode.util.Utils;
import java.awt.geom.Point2D;

// Designed By @RobyRew
public class WallsKiller extends Robot {
    double previousEnergy = 100;  // Inicia con la energía máxima de un robot

    public void run() {
        setColors(Color.red, Color.black, Color.yellow); // cuerpo, cañón, radar
        while(true) {
            turnGunRight(360); // Gira constantemente el cañón buscando enemigos
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        // Decidir la potencia del disparo basado en la distancia al oponente
        double bulletPower = Math.min(3, Math.max(400 / e.getDistance(), 1));
        double myX = getX();
        double myY = getY();
        double absoluteBearing = getHeading() + e.getBearing();

        // Predecir la posición futura del oponente
        double predictedX = myX + e.getDistance() * Math.sin(Math.toRadians(absoluteBearing));
        double predictedY = myY + e.getDistance() * Math.cos(Math.toRadians(absoluteBearing));

        double deltaTime = 0;
        double enemyHeading = e.getHeadingRadians();
        double enemyVelocity = e.getVelocity();

        while((++deltaTime) * (20 - 3 * bulletPower) < Point2D.distance(myX, myY, predictedX, predictedY)){
            predictedX += Math.sin(enemyHeading) * enemyVelocity;
            predictedY += Math.cos(enemyHeading) * enemyVelocity;
        }

        double theta = Utils.normalAbsoluteAngle(Math.atan2(predictedX - myX, predictedY - myY));
        turnGunRight(Utils.normalRelativeAngleDegrees(Math.toDegrees(theta) - getGunHeading()));

        fire(bulletPower);

        // Implementación de la estrategia de movimiento basada en la energía del oponente
        double changeInEnergy = previousEnergy - e.getEnergy();
        if (changeInEnergy > 0 && changeInEnergy <= 3) {
            // El oponente ha disparado
            smartMovement(e.getBearing());  // Mueve en respuesta al disparo
        }
        previousEnergy = e.getEnergy();
    }

    public void smartMovement(double enemyBearing) {
        // Se mueve perpendicular al oponente para esquivar
        turnRight(Utils.normalRelativeAngleDegrees(enemyBearing + 90));
        
        // Se aleja o acerca basado en la energía del oponente
        if (previousEnergy > 50) {
            ahead(150);
        } else {
            back(100);
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        // Cambia la dirección al ser golpeado
        smartMovement(e.getBearing());
    }

    public void onHitWall(HitWallEvent e) {
        // Cambia la dirección al chocar contra una pared
        turnLeft(180);
        ahead(100);
    }
}
