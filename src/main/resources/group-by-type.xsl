<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 <xsl:output method="xml" encoding="UTF-8" indent="yes"/>
 <xsl:strip-space elements="*"/>
 <xsl:key name="by-type" match="Item" use="Type"/>
 <xsl:template match="/Knife">
  <KnifeGroups>
   <xsl:for-each select="Item[generate-id() = generate-id(key('by-type', Type)[1])]">
    <xsl:sort select="Type"/>
    <Group type="{Type}"><xsl:copy-of select="key('by-type', Type)"/></Group>
   </xsl:for-each>
  </KnifeGroups>
 </xsl:template>
</xsl:stylesheet>
