package org.kiwiproject.net;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("KiwiCidrs")
class KiwiCidrsTest {

    @Test
    void shouldThrowIllegalArgumentExceptionIfNotValidCidr() {
        assertThatThrownBy(() -> new KiwiCidrs("1.2.3.4"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("not a valid CIDR format!");
    }

    @ParameterizedTest
    @CsvSource({
        "192.168.200.5/32, 192.168.200.5, 192.168.200.5",
        "192.168.200.5/24, 192.168.200.0, 192.168.200.255",
        "192.168.200.5/16, 192.168.0.0, 192.168.255.255",
        "192.168.200.5/8, 192.0.0.0, 192.255.255.255",
        "192.168.200.5/0, 0.0.0.0, 255.255.255.255",
        "0.0.0.0/0, 0.0.0.0, 255.255.255.255",
        "::/0, 0:0:0:0:0:0:0:0, ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff",
        "2000::/16, 2000:0:0:0:0:0:0:0, 2000:ffff:ffff:ffff:ffff:ffff:ffff:ffff"
    })
    void shouldParseCidr(String cidr, String networkAddress, String broadcastAddress) {
        var cidrs = new KiwiCidrs(cidr);

        assertThat(cidrs.getNetworkAddress()).isEqualTo(networkAddress);
        assertThat(cidrs.getBroadcastAddress()).isEqualTo(broadcastAddress);
    }

    @ParameterizedTest
    @CsvSource({
            "192.168.200.5/32, 192.168.200.5",
            "192.168.200.5/24, 192.168.200.25",
            "192.168.200.5/16, 192.168.150.50",
            "192.168.200.5/8, 192.100.150.5",
            "192.168.200.5/0, 10.10.1.1",
            "0.0.0.0/0, 10.10.1.1",
            "::/0, 2001:db8:0000:0000:0000:0000:0000:003f",
            "2000::/16, 2000:db8:0000:0000:0000:0000:0000:003f"
    })
    void shouldReturnTrueWhenAddressIsInRangeOfCidr(String cidr, String address) {
        var cidrs = new KiwiCidrs(cidr);

        assertThat(cidrs.isInRange(address)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "192.168.200.5/32, 192.168.200.10",
            "192.168.200.5/24, 192.168.201.25",
            "192.168.200.5/16, 192.200.150.50",
            "192.168.200.5/8, 10.100.150.5",
            "2000::/16, 2001:db8:0000:0000:0000:0000:0000:003f"
    })
    void shouldReturnFalseWhenAddressIsNotInRangeOfCidr(String cidr, String address) {
        var cidrs = new KiwiCidrs(cidr);

        assertThat(cidrs.isInRange(address)).isFalse();
    }
}
