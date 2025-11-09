package com.example.rocketreserver

import android.R.attr.contentDescription
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.exception.ApolloNetworkException
import com.example.rocketreserver.LaunchDetailsState.Loading
import com.example.rocketreserver.LaunchDetailsState.Success
import com.example.rocketreserver.LaunchDetailsState.Error

private sealed interface LaunchDetailsState {
    object Loading : LaunchDetailsState
    data class Error(val message: String) : LaunchDetailsState
    data class Success(val data: LaunchDetailsQuery.Data) : LaunchDetailsState
}

@Composable
fun LaunchDetails(launchId: String, navigateToLogin: () -> Unit) {
    var state by remember { mutableStateOf<LaunchDetailsState>(Loading) }
    LaunchedEffect(Unit) {
        val resp = apolloClient.query(LaunchDetailsQuery(launchId)).execute()
        state = when {
            resp.errors.orEmpty().isNotEmpty() -> {
                // GraphQL error
                Error(resp.errors!!.first().message)
            }
            resp.exception is ApolloNetworkException -> {
                // Network error
                Error("Please check your network connectivity.")
            }
            resp.data != null -> {
                // data (never partial)
                Success(resp.data!!)
            }
            else -> {
                // Another fetch error, maybe a cache miss?
                // Or potentially a non-compliant server returning data: null without an error
                Error("Oh no... An error happened.")
            }
        }
    }

    when (val s = state) {
        Loading -> Loading()
        is Error -> ErrorMessage(s.message)
        is Success -> LaunchDetails(s.data, navigateToLogin)
    }
}

@Composable
private fun LaunchDetails(
    data: LaunchDetailsQuery.Data,
    navigateToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Mission patch
            AsyncImage(
                modifier = Modifier.size(160.dp, 160.dp),
                model = data.launch?.mission?.missionPatch,
                placeholder = painterResource(R.drawable.ic_placeholder),
                error = painterResource(R.drawable.ic_placeholder),
                contentDescription = "Mission Patch"
            )

            Spacer(modifier = Modifier.size(16.dp))

            Column {
                // Mission name
                Text(
                    style = MaterialTheme.typography.headlineMedium,
                    text = data.launch?.mission?.name ?: "Mission N/A"
                )

                // Rocket name
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    text = data.launch?.rocket?.name?.let { "🚀 $it" } ?: "Rocket N/A",
                )

                // Site
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    text = data.launch?.site ?: "Site N/A"
                )
            }
        }

        // Book button
        Button(
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxWidth(),
            onClick = {
                onBookButtonClick(
                    launchId = data.launch?.id ?: "",
                    isBooked = data.launch?.isBooked == true,
                    navigateToLogin = navigateToLogin
                )
            }
        ) {
            Text(text = "Book now")
        }
    }
}

@Composable
private fun ErrorMessage(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text)
    }
}

@Composable
private fun Loading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SmallLoading() {
    CircularProgressIndicator(
        modifier = Modifier.size(24.dp),
        color = LocalContentColor.current,
        strokeWidth = 2.dp,
    )
}

private fun onBookButtonClick(launchId: String, isBooked: Boolean, navigateToLogin: () -> Unit): Boolean {
    if (TokenRepository.getToken() == null) {
        navigateToLogin()
        return false
    }

    if (isBooked) {
        // TODO Cancel booking
    } else {
        // TODO Book
    }
    return false
}

@Preview(showBackground = true)
@Composable
private fun LaunchDetailsPreview() {
//    LaunchDetails(launchId = "42")
}
