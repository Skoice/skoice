package net.clementraynaud.skoice.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworksTest {

    @Mock
    private Network network1;

    @Mock
    private Network network2;

    @BeforeEach
    void setUp() {
        Networks.clear();
    }

    @Test
    void testMergeNetworks() {
        when(this.network1.size()).thenReturn(5);
        when(this.network2.size()).thenReturn(3);

        Networks.merge(this.network1, this.network2);
        verify(this.network1).engulf(this.network2);

        reset(this.network1, this.network2);

        when(this.network1.size()).thenReturn(2);
        when(this.network2.size()).thenReturn(4);

        Networks.merge(this.network1, this.network2);
        verify(this.network2).engulf(this.network1);
    }
}