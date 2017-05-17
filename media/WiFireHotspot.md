## The **WiFireHotspot** object has information about the pubic WiFi hotspots

#### It has the following properties

```java
getId()
```
Returns an id which is unique for every hotspot. 
WiFire can also analyze networks and determine if they are public in real time. Id can be `null` if the network was detected in real time


```java
getName()
```
Returns the *name of the business/place* that the WiFi is associated with. Can be `null`.


```java
getSsid()
```
Returns the name of the WiFi network

```java
getLatitude()
```
Returns the *Latitude* of the hotspot's location

```java
getLongitude()
```
Returns the *Longitude* of the hotspot's location

```java
getRating()
```
Returns rating for the hotspot out of 5. This is a representation of the hotspot's quality of service and speed

```java
getAverageDownloadSpeed()
```
Returns an approximate download speed for the network in *bytes/second*

```java
getAverageUploadSpeed()
```
Returns an approximate upload speed for the network in *bytes/second*

```java
getSignalLevel(5)
```
Returns a number which denotes the signal level, here 5 is the maximum number of levels. *Useful for displaying signal level icon*