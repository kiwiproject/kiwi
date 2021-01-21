package org.kiwiproject.net;

import com.google.common.net.InetAddresses;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 * Small utility to analyze CIDR information.  Supports both IPv4 and IPv6.
 * <p>
 * This utility was copied and enhanced from
 * <a href="https://github.com/edazdarevic/CIDRUtils">https://github.com/edazdarevic/CIDRUtils</a> which has not been
 * updated since 2019 and seems to be unmaintained.
 */
@SuppressWarnings("UnstableApiUsage")
@Slf4j
public class CidrRange {

    private final InetAddress inetAddress;
    private final int prefixLength;

    private InetAddress startAddress;
    private InetAddress endAddress;
    private BigInteger startAddressBigInt;
    private BigInteger endAddressBigInt;

    /**
     * Creates a new instance of KiwiCidrs parsing the given CIDR.
     *
     * @param cidr the CIDR to use for this instance.
     */
    public CidrRange(String cidr) {

        /* split CIDR to address and prefix part */
        if (cidr.contains("/")) {
            var index = cidr.indexOf("/");
            var addressPart = cidr.substring(0, index);
            var networkPart = cidr.substring(index + 1);

            inetAddress = InetAddresses.forString(addressPart);
            prefixLength = Integer.parseInt(networkPart);

            calculate();
        } else {
            throw new IllegalArgumentException("not a valid CIDR format!");
        }
    }

    private void calculate() {

        ByteBuffer maskBuffer;
        int targetSize;
        if (inetAddress.getAddress().length == 4) {
            maskBuffer = ByteBuffer.allocate(4)
                    .putInt(-1);

            targetSize = 4;
        } else {
            maskBuffer = ByteBuffer.allocate(16)
                    .putLong(-1L)
                    .putLong(-1L);
            targetSize = 16;
        }

        var mask = (new BigInteger(1, maskBuffer.array())).not().shiftRight(prefixLength);

        var buffer = ByteBuffer.wrap(inetAddress.getAddress());
        var ipVal = new BigInteger(1, buffer.array());

        var startIp = ipVal.and(mask);
        var endIp = startIp.add(mask.not());

        this.startAddress = targetSize == 4 ? InetAddresses.fromIPv4BigInteger(startIp) : InetAddresses.fromIPv6BigInteger(startIp);
        this.startAddressBigInt = new BigInteger(1, this.startAddress.getAddress());

        this.endAddress = targetSize == 4 ? InetAddresses.fromIPv4BigInteger(endIp) : InetAddresses.fromIPv6BigInteger(endIp);
        this.endAddressBigInt = new BigInteger(1, this.endAddress.getAddress());

    }

    /**
     * Returns the network address for the CIDR.  For example: 192.168.100.15/24 will return 192.168.100.0.
     *
     * @return The network address for the CIDR
     */
    public String getNetworkAddress() {
        return this.startAddress.getHostAddress();
    }

    /**
     * Returns the broadcast address for the CIDR. For example: 192.168.100.15/24 will return 192.168.100.255.
     *
     * @return The broadcast address for the CIDR
     */
    public String getBroadcastAddress() {
        return this.endAddress.getHostAddress();
    }

    /**
     * Checks if a given IP address (as a string) is in the CIDR range.
     *
     * @param ipAddress the IP address to check
     * @return true if the IP address is in range, false otherwise
     */
    public boolean isInRange(String ipAddress) {
        var address = InetAddresses.forString(ipAddress);
        return isInRange(address);
    }

    /**
     * Checks if a given IP address (as an {@link InetAddress}) is in the CIDR range.
     *
     * @param address the IP address to check
     * @return true if the IP address is in range, false otherwise
     */
    public boolean isInRange(InetAddress address) {
        var target = new BigInteger(1, address.getAddress());

        if (startAddressBigInt.compareTo(target) > 0){
            return false; //start is higher than address -> is not in range
        }

        return endAddressBigInt.compareTo(target) >= 0; // end is higher or equal -> is in range
    }
}
