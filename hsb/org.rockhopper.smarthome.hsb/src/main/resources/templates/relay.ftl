{
  "name": "${wesRelay.name.value}",
  "stat_t": "wes2mqtt/stat/relay${wesRelay.index}/value",
  "cmd_t": "wes2mqtt/cmnd/relay${wesRelay.index}/value",
  "uniq_id": "wes_relay${wesRelay.index}",
  "pl_off": "0",
  "pl_on": "1",
  "frc_upd": "true",
  "dev": {
    "ids": "${wesServer.macAddress}",
    "name": "wes_${wesServer.macAddress}",
    "sw": "${wesServer.wesData.info.firmware.value}",
    "mdl": "F417 V2",
    "mf": "cartelectronic.fr"
  }
}
