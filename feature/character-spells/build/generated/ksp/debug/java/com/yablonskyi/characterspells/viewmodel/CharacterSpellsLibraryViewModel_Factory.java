package com.yablonskyi.characterspells.viewmodel;

import androidx.lifecycle.SavedStateHandle;
import com.yablonskyi.domain.repository.SpellRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class CharacterSpellsLibraryViewModel_Factory implements Factory<CharacterSpellsLibraryViewModel> {
  private final Provider<SpellRepository> repositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private CharacterSpellsLibraryViewModel_Factory(Provider<SpellRepository> repositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.repositoryProvider = repositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public CharacterSpellsLibraryViewModel get() {
    return newInstance(repositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static CharacterSpellsLibraryViewModel_Factory create(
      Provider<SpellRepository> repositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new CharacterSpellsLibraryViewModel_Factory(repositoryProvider, savedStateHandleProvider);
  }

  public static CharacterSpellsLibraryViewModel newInstance(SpellRepository repository,
      SavedStateHandle savedStateHandle) {
    return new CharacterSpellsLibraryViewModel(repository, savedStateHandle);
  }
}
