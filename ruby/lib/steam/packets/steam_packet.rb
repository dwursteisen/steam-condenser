# This code is free software; you can redistribute it and/or modify it under the
# terms of the new BSD License.
#
# Copyright (c) 2008-2010, Sebastian Staudt

require 'stringio_additions'

# This class represents a packet used by the Source query protocol
class SteamPacket

  A2A_ACK_HEADER = 0x6A
  A2A_PING_HEADER = 0x69
  A2M_GET_SERVERS_BATCH2_HEADER = 0x31
  A2S_INFO_HEADER = 0x54
  A2S_PLAYER_HEADER = 0x55
  A2S_RULES_HEADER = 0x56
  A2S_SERVERQUERY_GETCHALLENGE_HEADER = 0x57
  M2A_SERVER_BATCH_HEADER = 0x66
  RCON_GOLDSRC_CHALLENGE_HEADER = 0x63
  RCON_GOLDSRC_NO_CHALLENGE_HEADER = 0x39
  RCON_GOLDSRC_RESPONSE_HEADER = 0x6c
  S2A_INFO_DETAILED_HEADER = 0x6D
  S2A_INFO2_HEADER = 0x49
  S2A_PLAYER_HEADER = 0x44
  S2A_RULES_HEADER = 0x45
  S2C_CONNREJECT_HEADER = 0x39
  S2C_CHALLENGE_HEADER = 0x41

  # Creates a new SteamPacket object with given header and content data
  def initialize(header_data, content_data = '')
    @content_data = StringIO.new content_data.to_s
    @header_data  = header_data
  end

  # Returns a packed string representing the packet's data
  def to_s
    [0xFF, 0xFF, 0xFF, 0xFF, @header_data, @content_data.string].pack('c5a*')
  end

end
