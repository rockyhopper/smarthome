{
	"name": "${wesSensor.name.value}",
	"stat_t": "wes2mqtt/stat/sensor${wesSensor.index}/value",
	"uniq_id": "wes_sensor${wesSensorIdxPlusOne}_temperature",
	"object_id": "wes_sensor${wesSensorIdxPlusOne}",
	"unit_of_meas": "°C",
	"dev_cla": "temperature",
	"stat_cla": "measurement",
	"dev": {
		"ids": "${wesSensor.prettyId}",
		"name": "wes_sensor_${wesSensor.prettyId}",
		"mdl": "${wesSensor.type.value}",
		"mf": "cartelectronic.fr"
	}
}
