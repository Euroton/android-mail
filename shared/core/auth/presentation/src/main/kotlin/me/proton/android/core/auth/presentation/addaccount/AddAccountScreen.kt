/*
 * Copyright (C) 2024 Proton AG
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.android.core.auth.presentation.addaccount

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import me.proton.android.core.auth.presentation.R
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.ProtonTypography
import me.proton.core.compose.theme.defaultSmallWeak
import me.proton.core.presentation.R as CoreR

public const val SMALL_SCREEN_HEIGHT: Int = 680

@Composable
public fun AddAccountScreenMail(
    modifier: Modifier = Modifier,
    serverDomain: String? = null,
    onServerChosen: ((String?, CustomServer.ServerKey?) -> Unit)? = null,
    onSignUpClicked: () -> Unit = {},
    onSignInClicked: () -> Unit = {}
) {
    AddAccountScreen(
        appHeader = R.drawable.welcome_header_mail,
        appLogoWithName = CoreR.drawable.logo_mail_with_text,
        appLogoContentDescription = R.string.app_name_mail,
        appSubtitle = R.string.welcome_subtitle_mail,
        serverDomain = serverDomain,
        onServerChosen = onServerChosen,
        onSignUpClicked = onSignUpClicked,
        onSignInClicked = onSignInClicked,
        modifier = modifier
    )
}

@Composable
public fun AddAccountScreen(
    @DrawableRes appHeader: Int,
    @DrawableRes appLogoWithName: Int,
    @StringRes appLogoContentDescription: Int,
    @StringRes appSubtitle: Int,
    modifier: Modifier = Modifier,
    serverDomain: String? = null,
    onServerChosen: ((String?, CustomServer.ServerKey?) -> Unit)? = null,
    onSignUpClicked: () -> Unit = {},
    onSignInClicked: () -> Unit = {}
) {
    BoxWithConstraints(modifier = modifier.background(LocalColors.current.backgroundNorm)) {
        val isSmallHeight = maxHeight <= SMALL_SCREEN_HEIGHT.dp

        ConstraintLayout(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets
                        .safeDrawing
                        .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                )
        ) {
            val (header, content, footer) = createRefs()
            val (bottomImageSpacer, middleSpacer) = createRefs()
            val contentTopBarrier = createTopBarrier(bottomImageSpacer, middleSpacer)
            val middleGuideline = createGuidelineFromTop(0.5f)
            val bottomGuideline = createGuidelineFromBottom(ProtonDimens.MediumSpacing)

            Image(
                painter = painterResource(appHeader),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(header) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        if (this@BoxWithConstraints.maxWidth > this@BoxWithConstraints.maxHeight) {
                            top.linkTo(parent.top)
                            bottom.linkTo(content.top)
                        } else {
                            top.linkTo(parent.top)
                        }
                    },
                contentScale = ContentScale.FillWidth,
                alignment = Alignment.BottomCenter
            )

            Spacer(
                modifier = Modifier.constrainAs(middleSpacer) {
                    top.linkTo(middleGuideline)
                }
            )
            Spacer(
                modifier = Modifier.constrainAs(bottomImageSpacer) {
                    top.linkTo(header.bottom)
                }
            )

            AddAccountScreenContent(
                appLogoWithNamePainter = painterResource(appLogoWithName),
                appNameContentDescription = stringResource(appLogoContentDescription),
                subtitleText = stringResource(appSubtitle),
                serverDomain = serverDomain,
                onServerChosen = onServerChosen,
                onSignUpClicked = onSignUpClicked,
                onSignInClicked = onSignInClicked,
                modifier = Modifier
                    .padding(horizontal = ProtonDimens.LargeSpacing)
                    .widthIn(max = 360.dp)
                    .constrainAs(content) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        if (isSmallHeight) {
                            bottom.linkTo(parent.bottom, margin = ProtonDimens.MediumSpacing)
                        } else {
                            top.linkTo(contentTopBarrier, margin = ProtonDimens.LargeSpacing)
                        }
                    }
            )

            if (!isSmallHeight) {
                Image(
                    modifier = Modifier
                        .constrainAs(footer) {
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(bottomGuideline)
                        },
                    painter = painterResource(id = R.drawable.logo_proton_footer),
                    colorFilter = ColorFilter.tint(LocalColors.current.textHint),
                    contentDescription = stringResource(R.string.welcome_proton_privacy_by_default),
                    alignment = Alignment.Center
                )
            }
        }
    }
}

@Composable
private fun AddAccountScreenContent(
    appLogoWithNamePainter: Painter,
    appNameContentDescription: String,
    subtitleText: String,
    serverDomain: String?,
    onServerChosen: ((String?, CustomServer.ServerKey?) -> Unit)?,
    onSignUpClicked: () -> Unit,
    onSignInClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = appLogoWithNamePainter,
            contentDescription = appNameContentDescription,
            alignment = Alignment.Center
        )

        Text(
            text = subtitleText,
            style = ProtonTypography.Default.defaultSmallWeak,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = ProtonDimens.MediumSpacing)
        )

        ProtonSolidButton(
            contained = false,
            onClick = onSignUpClicked,
            modifier = Modifier
                .padding(top = ProtonDimens.MediumSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight)
        ) {
            Text(text = stringResource(R.string.welcome_sign_up))
        }

        ProtonTextButton(
            contained = false,
            onClick = onSignInClicked,
            modifier = Modifier
                .padding(vertical = ProtonDimens.DefaultSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight)
        ) {
            Text(text = stringResource(R.string.welcome_sign_in))
        }

        if (onServerChosen != null) {
            ServerRow(serverDomain = serverDomain, onServerChosen = onServerChosen)
        }
    }
}

@Composable
private fun ServerRow(serverDomain: String?, onServerChosen: (String?, CustomServer.ServerKey?) -> Unit) {
    var isDialogShown by rememberSaveable { mutableStateOf(false) }

    ProtonTextButton(
        contained = false,
        onClick = { isDialogShown = true },
        modifier = Modifier.height(ProtonDimens.DefaultButtonMinHeight)
    ) {
        Text(text = stringResource(R.string.server_row, serverDomain ?: stringResource(R.string.server_default)))
    }

    if (isDialogShown) {
        ServerDialog(
            serverDomain = serverDomain,
            onServerChosen = onServerChosen,
            onDismiss = { isDialogShown = false }
        )
    }
}

@Composable
private fun ServerDialog(
    serverDomain: String?,
    onServerChosen: (String?, CustomServer.ServerKey?) -> Unit,
    onDismiss: () -> Unit
) {
    var input by rememberSaveable { mutableStateOf(serverDomain.orEmpty()) }
    var isInvalid by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var fetchError by remember { mutableStateOf<String?>(null) }
    var pending by remember { mutableStateOf<Pair<String, CustomServer.ServerKey>?>(null) }
    val scope = rememberCoroutineScope()

    val confirm = pending
    if (confirm != null) {
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text(text = stringResource(R.string.server_key_title)) },
            text = {
                Column {
                    Text(text = stringResource(R.string.server_key_message, confirm.first))
                    Text(
                        text = CustomServer.formatFingerprint(confirm.second.fingerprint),
                        style = ProtonTypography.Default.defaultSmallWeak,
                        modifier = Modifier.padding(top = ProtonDimens.DefaultSpacing)
                    )
                }
            },
            confirmButton = {
                ProtonTextButton(onClick = { onServerChosen(confirm.first, confirm.second) }) {
                    Text(text = stringResource(R.string.server_key_trust))
                }
            },
            dismissButton = {
                ProtonTextButton(onClick = { pending = null }) {
                    Text(text = stringResource(R.string.server_key_cancel))
                }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.server_dialog_title)) },
        text = {
            Column {
                Text(text = stringResource(R.string.server_dialog_message))
                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        input = it
                        isInvalid = false
                        fetchError = null
                    },
                    singleLine = true,
                    isError = isInvalid || fetchError != null,
                    enabled = !isLoading,
                    label = { Text(text = stringResource(R.string.server_dialog_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = ProtonDimens.DefaultSpacing)
                )
                if (isInvalid) {
                    Text(
                        text = stringResource(R.string.server_dialog_invalid),
                        style = ProtonTypography.Default.defaultSmallWeak
                    )
                }
                fetchError?.let {
                    Text(
                        text = stringResource(R.string.server_key_fetch_failed, it),
                        style = ProtonTypography.Default.defaultSmallWeak
                    )
                }
            }
        },
        confirmButton = {
            ProtonTextButton(
                onClick = {
                    val domain = CustomServer.normalize(input)
                    if (domain == null) {
                        isInvalid = true
                    } else {
                        isLoading = true
                        scope.launch {
                            CustomServer.fetchKey(domain)
                                .onSuccess { pending = domain to it }
                                .onFailure { fetchError = it.message ?: it.javaClass.simpleName }
                            isLoading = false
                        }
                    }
                }
            ) { Text(text = stringResource(R.string.server_dialog_save)) }
        },
        dismissButton = {
            ProtonTextButton(onClick = { onServerChosen(null, null) }) {
                Text(text = stringResource(R.string.server_dialog_use_default))
            }
        }
    )
}

@Preview(name = "Light mode")
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun WelcomeScreenMailPreview() {
    ProtonTheme {
        AddAccountScreenMail()
    }
}
