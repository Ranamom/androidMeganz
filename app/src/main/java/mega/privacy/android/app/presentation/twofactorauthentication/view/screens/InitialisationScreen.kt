package mega.privacy.android.app.presentation.twofactorauthentication.view.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import mega.privacy.android.app.R
import mega.privacy.android.core.ui.controls.buttons.RaisedDefaultMegaButton
import mega.privacy.android.core.ui.preview.CombinedThemePreviews
import mega.privacy.android.core.ui.theme.extensions.grey_020_grey_800
import mega.privacy.android.core.ui.theme.extensions.subtitle1medium
import mega.privacy.android.core.ui.theme.extensions.textColorPrimary
import mega.privacy.android.core.ui.theme.extensions.textColorSecondary

@Composable
fun InitialisationScreen(
    onSetupBeginClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.grey_020_grey_800)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_2fa),
                contentDescription = "",
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 32.dp, bottom = 32.dp)
            )
        }
        Spacer(modifier = Modifier.padding(top = 20.dp))
        Text(
            text = stringResource(id = R.string.title_2fa),
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.textColorPrimary,
            style = MaterialTheme.typography.subtitle1medium
        )
        Spacer(modifier = Modifier.padding(top = 20.dp))
        Text(
            text = stringResource(id = R.string.two_factor_authentication_explain),
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.textColorSecondary,
            style = MaterialTheme.typography.subtitle1,
        )
        Spacer(modifier = Modifier.padding(top = 32.dp))
        RaisedDefaultMegaButton(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(),
            textId = R.string.button_setup_2fa,
            onClick = {
                onSetupBeginClicked()
            }
        )
    }
}

@CombinedThemePreviews
@Composable
fun PreviewInitialisationScreen() {
    InitialisationScreen {}
}