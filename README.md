# TPE1: Reservas de Atracciones de Parques Temáticos

Trabajo práctico especial de Programación de Objetos Distribuidos en el que se implementa
un sistema remoto thread safe para la reserva de atracciones de un parque temático en
un año, permitiendo notificar a los usuarios del servicio y ofreciendo reportes de las
reservas realizadas al momento.

## Autores
- [Tomas Alvarez Escalante](https://github.com/tomalvarezz)
- [Lucas Agustin Ferreiro](https://github.com/lukyferreiro)
- [Roman Gomez Kiss](https://github.com/rgomezkiss)

## Compilación

Para compilar el proyecto, se debe ejecutar el siguiente comando en la raíz del proyecto:

```bash
mvn clean install
```

Tras esto, en las carpetas /client/target y /server/target se generarán tp1-g5-client-1.0-SNAPSHOT-bin.tar.gz y tp1-g5-server-1.0-SNAPSHOT-bin.tar.gz
Dichos .tar.gz, los cuales se deberán descomprimir utilizando el siguiente comando:

```bash
tar -xzvf <nombre-del-archivo>.tar.gz
```

## Ejecución

Tras haber realizado lo anterior, podremos ejecutar el siguiente servicio y los clientes. Asegurarse de otorgar permisos de ejecución a los ejecutables.

### Servicios

El sistema cuenta con los siguientes servicios remotos:

- **Servicio de Administración del Parque**: administra las atracciones y pases de atracciones de un parque para un año determinado.
- **Servicio de Reserva de Atracciones**.
- **Servicio de Notificaciones de una Atracción**: recibe notificaciones respecto a los cambios en las reservas de una atracción del parque.
- **Servicio de Consulta**: consulta las reservas pendientes y confirmadas de las atracciones.

```sh
./server.sh
```

### Clientes

Para probar el sistema, se implementan cuatro programas cliente (cada uno coincidente con cada servicio remoto).

#### Cliente de Administración del Parque

```sh
./admin-cli.sh -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName [ -DinPath=filename | -Dride=rideName | -Dday=dayOfYear | -Dcapacity=amount ]
```

| Argumento          | Descripción                                                                                                   |
|--------------------|---------------------------------------------------------------------------------------------------------------|
| `-DserverAddress`  | Dirección IP y puerto donde esta publicado el Servicio de Administración del Parque                           |
| `-Daction`         | Acción a realizar por el cliente, puede tomar el valor: **rides**, **tickets** o **slots**                    |
| `-DinPath`         | Path al archivo CSV con lote de atracciones (para `-Daction=rides`) o lote de pases (para `-Daction=tickets`) |
| `-Dride`           | Nombre de la atracción (para `-Daction=slots`)                                                                |
| `-Dday`            | Dia del año (para `-Daction=slots`)                                                                           |
| `-Dcapacity`       | Capacidad a cargar de los slots de la atracción (para `-Daction=slots`)                                       |

- **rides**: Agrega un lote de atracciones.
- **tickets**: Agrega un lote de pases.
- **slots**: Carga la capacidad *amount* de los slots de la atracción con nombre *rideName* para el día del año *dayOfYear*.

#### Cliente de Reserva de Atracciones

```sh
./book-cli.sh -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName [ -Dday=dayOfYear -Dride=rideName -Dvisitor=visitorId -Dslot=bookingSlot -DslotTo=bookingSlotTo ]
```

| Argumento         | Descripción                                                                                                                   |
|-------------------|-------------------------------------------------------------------------------------------------------------------------------|
| `-DserverAddress` | Dirección IP y puerto donde esta publicado el Servicio de Reserva de Atracciones                                              |
| `-Daction`        | Acción a realizar por el cliente, puede tomar el valor: **attractions**, **availability**, **book**, **confirm** o **cancel** |
| `-Dday`           | Dia del año (para `-Daction=availability`, `-Daction=book`, `-Daction=confirm` o `-Daction=cancel`)                           |
| `-Dride`          | Nombre de la atracción (para `-Daction=availability`, `-Daction=book`, `-Daction=confirm` o `-Daction=cancel`)                |
| `-Dvisitor`       | Id del visitante que realiza la reserva (para `-Daction=book`, `-Daction=confirm` o `-Daction=cancel`)                        |
| `-Dslot`          | Slot de inicio en formato HH:MM (para `-Daction=availability`, `-Daction=book`, `-Daction=confirm` o `-Daction=cancel`)       |
| `-DslotTo`        | Slot de fin en formato HH:MM (para `-Daction=availability`)                                                                   |

- **attractions**: imprime en pantalla el detalle de las atracciones.
- **availability**: imprime en pantalla la disponibilidad de las atracciones para el día del año *dayOfYear* a partir de uno de los siguientes criterios:
  - Un slot de una atracción a partir del nombre de la atracción *rideName* y el slot *bookingSlot*.
  - Un rango de slots de una atracción a partir del nombre de la atracción *rideName* y el rango de slots *bookingSlot* - *bookingSlotTo*.
  - Un rango de slots de todas las atracciones del parque a partir del rango de slots *bookingSlot* - *bookingSlotTo*.
- **book**: realiza una reserva para el visitante *visitorId* para visitar la atracción *rideName* en el día del año *dayOfYear* en el slot *bookingSlot*.
- **confirm**: confirma una reserva para el visitante *visitorId* para visitar la atracción *rideName* en el día del año *dayOfYear* en el slot *bookingSlot*.
- **cancel**: cancela una reserva para el visitante *visitorId* para visitar la atracción *rideName* en el día del año *dayOfYear* en el slot *bookingSlot*.


#### Cliente de Notificaciones de una Atracción

```sh
./notif-cli.sh -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName -Dday=dayOfYear -Dride=rideName -Dvisitor=visitorId
```

| Argumento         | Descripción                                                                               |
|-------------------|-------------------------------------------------------------------------------------------|
| `-DserverAddress` | Dirección IP y puerto donde esta publicado el Servicio de Notificaciones de una Atracción |
| `-Daction`        | Acción a realizar por el cliente, puede tomar el valor: **follow** o **unfollow**         |
| `-Dday`           | Dia del año                                                                               |
| `-Dride`          | Nombre de la atracción sobre la cual se notifica                                          |
| `-Dvisitor`       | Id del visitante                                                                          |

- **follow**: registra a un visitante para que sea notificado de los eventos de una atracción.
- **unfollow**: anula el registro, es decir, deja de recibir notificaciones a partir de los mismos parámetros que la acción **follow**.

#### Cliente de Consulta

```sh
./query-cli.sh -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName -Dday=dayOfYear -DoutPath=output.txt 
```

| Argumento         | Descripción                                                                          |
|-------------------|--------------------------------------------------------------------------------------|
| `-DserverAddress` | Dirección IP y puerto donde esta publicado el Servicio de Consulta                   |
| `-Daction`        | Acción a realizar por el cliente, puede tomar el valor: **capacity** o **confirmed** |
| `-Dday`           | Dia del año                                                                          |
| `-DoutPath`       | Path al archivo de salida .txt con los resultados de la consulta                     |

- **capacity**: consulta cada atracción con su capacidad y el slot correspondiente a esa cantidad maxima.
- **confirmed**: consulta cada reserva con el id del visitante, para que atracción y en que slot.
