{
  "name": "${oneWireRelay.name.value}",
  "stat_t": "wes2mqtt/stat/relaysCards/card${wesRelaysCard.index}/relay${oneWireRelayIdxMinusOne}/state",
  "cmd_t": "wes2mqtt/cmnd/relaysCards/card${wesRelaysCard.index}/relay${oneWireRelayIdxMinusOne}/state",
  "uniq_id": "wes_relaysCards_card${wesRelaysCard.index}_relay${oneWireRelay.index}",
  "object_id": "wes_relay1${wesRelaysCard.index}${oneWireRelay.index}",
  "pl_off": "false",
  "pl_on": "true",
  "frc_upd": "true",
  "dev": {
    "ids": "${wesRelaysCard.prettyId}",
    "name": "wes_relayscard_${wesRelaysCard.prettyId}",
    "mdl": "1W8RLRD",
    "mf": "cartelectronic.fr"
  }
}
